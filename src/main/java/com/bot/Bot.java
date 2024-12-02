package com.bot;

import com.bot.db.*;
import com.bot.metrics.MetricsManager;
import com.bot.models.BannedImage;
import com.bot.models.InternalGuild;
import com.bot.models.InternalShard;
import com.bot.models.UsageLevel;
import com.bot.tasks.AddFreshGuildDeferredTask;
import com.bot.tasks.LeaveGuildDeferredTask;
import com.bot.tasks.ResumeAudioTask;
import com.bot.utils.AliasUtils;
import com.bot.utils.HttpUtils;
import com.bot.utils.Logger;
import com.bot.utils.VinnyConfig;
import com.bot.voice.GuildVoiceConnection;
import com.bot.voice.GuildVoiceProvider;
import com.jagrosh.jdautilities.command.impl.CommandClientImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class Bot extends ListenerAdapter {
	private final Logger LOGGER;

	private final VinnyConfig config;

	private final GuildDAO guildDAO;
	private final MembershipDAO membershipDAO;
	private final ChannelDAO channelDAO;
	private final BannedImageDAO bannedImageDAO;
	private final UserDAO userDAO;
	private final MetricsManager metricsManager;
	private final ExecutorService executor;
	private final GuildVoiceProvider guildVoiceProvider;

	public final static String SUPPORT_INVITE_LINK = "https://discord.gg/XMwyzxZ";


	Bot() {
		this.config = VinnyConfig.Companion.instance();

		guildDAO = GuildDAO.getInstance();
		membershipDAO = MembershipDAO.getInstance();
		channelDAO = ChannelDAO.getInstance();
		userDAO = UserDAO.getInstance();
		bannedImageDAO = BannedImageDAO.getInstance();

		LOGGER =  new Logger(Bot.class.getName());
		metricsManager = MetricsManager.Companion.getInstance();
		executor = Executors.newCachedThreadPool();
		guildVoiceProvider = GuildVoiceProvider.Companion.getInstance();
	}

	@Override
	public void onReady(ReadyEvent event) {
		ShardingManager shardingManager = ShardingManager.getInstance();
		shardingManager.putShard(new InternalShard(event.getJDA()));
		LOGGER.info("Shard: " + event.getJDA().getShardInfo().getShardId() + " ready");

		if (config.getBotConfig().getDataLoader()) {
			DataLoader.LoadThread t = null;
			try {
				t = new DataLoader.LoadThread(event.getJDA(), System.currentTimeMillis());
				t.start();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		executor.submit(new ResumeAudioTask(event));

		super.onReady(event);
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if ((event.isFromType(ChannelType.TEXT) || event.isFromType(ChannelType.VOICE)) && event.isFromGuild()) {
			onGuildMessageReceived(event);
		}
		super.onMessageReceived(event);
	}

	public void onGuildMessageReceived(MessageReceivedEvent event) {
		executor.execute(() -> {
			InternalGuild guild = guildDAO.getGuildById(event.getGuild().getId());
			if (guild == null) {
				LOGGER.warning("Guild not present when message received");
				guildDAO.addFreshGuild(event.getGuild());
				guild = guildDAO.getGuildById(event.getGuild().getId(), false);
			}
			MessageReceivedEvent aliasEvent = AliasUtils.getAliasMessageEvent(event, guild, null, null);

			if (aliasEvent != null) {
				// Alias matched, send it!
				CommandClientImpl client = ShardingManager.getInstance().getCommandClientImpl();
				// Null check cause we need to get the impl class here
				if (client != null) {
					// Execute the forged alias command
					client.onEvent(aliasEvent);
				}
				else {
					LOGGER.warning("Command client was null???");
				}
			}

			// Experimental, testing banned images based on neuralhash
			if (event.getMessage().getAttachments().size() > 0 && event.getMessage().getAttachments().get(0).isImage()) {
				// Scan for banned image
				List<BannedImage> bannedImages = bannedImageDAO.getAllInGuild(event.getGuild().getId());
				if (bannedImages.size() > 0) {
					String hash = HttpUtils.getHashforImage(event.getMessage().getAttachments().get(0).getUrl());
					if (bannedImages.stream().anyMatch(i -> i.getHash().equals(hash))) {
						// Banned image found, delete
						event.getMessage().delete().queue();
					}
				}
			}

		});
	}

	@Override
	public void onGenericEvent(@NotNull GenericEvent event) {
		metricsManager.markDiscordEvent(event.getJDA().getShardInfo().getShardId());
		super.onGenericEvent(event);
	}

	@Override
	public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
		executor.execute(() -> checkVoiceLobby(event));
		super.onGuildVoiceUpdate(event);
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		executor.execute(() -> membershipDAO.addUserToGuild(event.getUser(), event.getGuild()));
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		executor.execute(() -> membershipDAO.removeUserMembershipToGuild(event.getUser().getId(), event.getGuild().getId()));
		super.onGuildMemberRemove(event);
	}

	@Override
	public void onGuildJoin(GuildJoinEvent guildJoinEvent) {
		executor.submit(new AddFreshGuildDeferredTask(guildJoinEvent));
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent guildLeaveEvent) {
		executor.submit(new LeaveGuildDeferredTask(guildLeaveEvent));
	}

	@Override
	public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
		// Hardcoded for now since not much time to work on project, and really needs a rework anyways.
		// Checks for donor role assigned on support server

		// TODO: Generify this into some Eval level from roles for member helper
		if (event.getGuild().getId().equals("294900956078800897")) {
			String assignedId = event.getRoles().get(0).getId();
			if (assignedId.equals("638201929591423010") || assignedId.equals("638202019152265226") || assignedId.equals("1106473246230073364")) {
				try {
					userDAO.setUsageLevel(UsageLevel.DONOR, event.getUser().getId());
				} catch (SQLException throwable) {
					LOGGER.log(Level.SEVERE, "Failed to assign usage level", throwable);
				}
			}
			else if (assignedId.equals("1106638822252490832") || assignedId.equals("1106473242551664651")) {
				try {
					userDAO.setUsageLevel(UsageLevel.UNLIMITED, event.getUser().getId());
				} catch (SQLException throwable) {
					LOGGER.log(Level.SEVERE, "Failed to assign usage level", throwable);
				}
			}
		}
		super.onGuildMemberRoleAdd(event);
	}

	@Override
	public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
		// Hardcoded for now since not much time to work on project, and really needs a rework anyways.
		// Checks for donor role removed on support server

		// TODO: Generify this into some Eval level from roles for member helper
		if (event.getGuild().getId().equals("294900956078800897")) {
			String assignedId = event.getRoles().get(0).getId();
			if (assignedId.equals("638201929591423010")
					|| assignedId.equals("638202019152265226")
					|| assignedId.equals("1106473242551664651")
					|| assignedId.equals("1106473246230073364")) {
				try {
					userDAO.setUsageLevel(UsageLevel.BASIC, event.getUser().getId());
					membershipDAO.removeAllPremium(event.getUser().getId());
				} catch (SQLException throwables) {
					LOGGER.log(Level.SEVERE, "Failed to assign usage level", throwables);
				}
			}
		}
		super.onGuildMemberRoleRemove(event);
	}

	@Override
	public void onChannelCreate(@NotNull ChannelCreateEvent event) {
		if (event.isFromType(ChannelType.TEXT) && event.isFromGuild()) {
			onTextChannelCreate(event);
		} else if (event.isFromGuild() && event.isFromType(ChannelType.VOICE)) {
			onVoiceChannelCreate(event);
		}

		super.onChannelCreate(event);
	}

	public void onTextChannelCreate(ChannelCreateEvent event) {
		executor.execute(() -> channelDAO.addTextChannel(event.getChannel().asTextChannel()));
	}

	public void onVoiceChannelCreate(ChannelCreateEvent event) {
		executor.execute(() -> channelDAO.addVoiceChannel(event.getChannel().asVoiceChannel()));
	}

	@Override
	public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
		if (event.isFromGuild() && event.isFromType(ChannelType.TEXT)) {
			onTextChannelDelete(event);
		} else if (event.isFromGuild() && event.isFromType(ChannelType.VOICE)) {
			onVoiceChannelDelete(event);
		}

		super.onChannelDelete(event);
	}

	public void onTextChannelDelete(ChannelDeleteEvent event) {
		executor.execute(() -> {
			try {
				ScheduledCommandDAO.getInstance().removeAllScheduledInChannel(event.getChannel().getId());
				RssDAO.getInstance().removeAllSubsInChannel(event.getChannel().getId());
				channelDAO.removeTextChannel(event.getChannel().asTextChannel());
			} catch (SQLException e) {
				LOGGER.warning("Ran into error when removing text channel from db", e);
			}
		});
	}

	public void onVoiceChannelDelete(ChannelDeleteEvent event) {
		executor.execute(() -> channelDAO.removeVoiceChannel(event.getChannel().asVoiceChannel()));
	}

	@Override
	public void onChannelUpdateName(@NotNull ChannelUpdateNameEvent event) {
		if (event.isFromGuild() && event.isFromType(ChannelType.TEXT)) {
			onTextChannelUpdateName(event);
		} else if (event.isFromGuild() && event.isFromType(ChannelType.VOICE)) {
			onVoiceChannelUpdateName(event);
		}

		super.onChannelUpdateName(event);
	}

	public void onTextChannelUpdateName(ChannelUpdateNameEvent event) {
		executor.execute(() -> channelDAO.addTextChannel(event.getChannel().asTextChannel()));
	}

	public void onVoiceChannelUpdateName(ChannelUpdateNameEvent event) {
		executor.execute(() -> channelDAO.addVoiceChannel(event.getChannel().asVoiceChannel()));
	}

	@Override
	public void onShutdown(@NotNull ShutdownEvent event) {
		executor.shutdown();
		super.onShutdown(event);
	}

	private void checkVoiceLobby(GuildVoiceUpdateEvent event) {
		Guild guild = event.getGuild();
		GuildVoiceConnection conn = guildVoiceProvider.getGuildVoiceConnection(guild.getIdLong());
		if (conn == null || event.getChannelLeft() != conn.getCurrentVoiceChannel()) {
			return;
		}
		if (event.getMember().equals(guild.getSelfMember())) {
			if (event.getChannelJoined() == null) {
				// Kicked from voice, maybe message or save tmp playlist
				conn.cleanupPlayer();
				return;
			}
			// update our currently playing channel if there are people in there
			conn.setCurrentVoiceChannel(event.getChannelJoined().asVoiceChannel());
		}

		// if there are no humans left, then leave
		int users = 0;
		for (Member member : conn.getCurrentVoiceChannel().getMembers()) {
			if (!member.getUser().isBot())
				users++;
		}
		if (users < 1) {
			conn.sendMessageToChannel("Leaving voice, no one is in the channel.");
			conn.cleanupPlayer();
		}
	}
}
