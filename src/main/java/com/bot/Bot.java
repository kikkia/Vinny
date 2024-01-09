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
import com.bot.utils.*;
import com.bot.voice.GuildVoiceConnection;
import com.bot.voice.GuildVoiceProvider;
import com.jagrosh.jdautilities.command.impl.CommandClientImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class Bot extends ListenerAdapter {
	private final Logger LOGGER;

	private Config config;

	private GuildDAO guildDAO;
	private MembershipDAO membershipDAO;
	private ChannelDAO channelDAO;
	private BannedImageDAO bannedImageDAO;
	private UserDAO userDAO;
	private MetricsManager metricsManager;
	private ExecutorService executor;
	private GuildVoiceProvider guildVoiceProvider;

	public final static String SUPPORT_INVITE_LINK = "https://discord.gg/XMwyzxZ";


	Bot() {
		this.config = Config.getInstance();

		guildDAO = GuildDAO.getInstance();
		membershipDAO = MembershipDAO.getInstance();
		channelDAO = ChannelDAO.getInstance();
		userDAO = UserDAO.getInstance();
		bannedImageDAO = BannedImageDAO.getInstance();

		LOGGER =  new Logger(Bot.class.getName());
		metricsManager = MetricsManager.Companion.getInstance();
		executor = Executors.newScheduledThreadPool(60);
		guildVoiceProvider = GuildVoiceProvider.Companion.getInstance();
	}

	@Override
	public void onReady(ReadyEvent event) {
		ShardingManager shardingManager = ShardingManager.getInstance();
		shardingManager.putShard(new InternalShard(event.getJDA()));
		LOGGER.info("Shard: " + event.getJDA().getShardInfo().getShardId() + " ready");

		if (Boolean.parseBoolean(config.getConfig(Config.DATA_LOADER))) {
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
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
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
		super.onGuildMessageReceived(event);
	}

	@Override
	public void onGenericEvent(@NotNull GenericEvent event) {
		metricsManager.markDiscordEvent(event.getJDA().getShardInfo().getShardId());
		super.onGenericEvent(event);
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		executor.execute(() -> checkVoiceLobby(event));
	}

	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		executor.execute(() -> checkVoiceLobby(event));
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		executor.execute(() -> membershipDAO.addUserToGuild(event.getUser(), event.getGuild()));
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		executor.execute(() -> membershipDAO.removeUserMembershipToGuild(event.getUser().getId(), event.getGuild().getId()));
	}

	@Override
	public void onGuildJoin(GuildJoinEvent guildJoinEvent) {
		// In some cases (large guilds) this can take a while, so put it on its own thread
		AddFreshGuildDeferredTask deferredTask = new AddFreshGuildDeferredTask(guildJoinEvent);
		deferredTask.start();

		// If we are posting stats to external discord bot sites, then do it async
		executor.execute(() -> {
			if (Boolean.parseBoolean(config.getConfig(Config.ENABLE_EXTERNAL_APIS)))
				HttpUtils.postGuildCountToExternalSites();
		});
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent guildLeaveEvent) {
		// In some cases (large guilds) this can take a while, so put it on its own thread
		LeaveGuildDeferredTask deferredTask = new LeaveGuildDeferredTask(guildLeaveEvent);
		deferredTask.start();

		// If we are posting stats to external discord bot sites, then do it async
		executor.execute(() -> {
			if (Boolean.parseBoolean(config.getConfig(Config.ENABLE_EXTERNAL_APIS)))
				HttpUtils.postGuildCountToExternalSites();
		});
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
			else if (assignedId.equals("1106473242551664651")) {
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
				} catch (SQLException throwables) {
					LOGGER.log(Level.SEVERE, "Failed to assign usage level", throwables);
				}
			}
		}
		super.onGuildMemberRoleRemove(event);
	}

	@Override
	public void onTextChannelCreate(TextChannelCreateEvent event) {
		executor.execute(() -> channelDAO.addTextChannel(event.getChannel()));
	}

	@Override
	public void onVoiceChannelCreate(VoiceChannelCreateEvent event) {
		executor.execute(() -> channelDAO.addVoiceChannel(event.getChannel()));
	}

	@Override
	public void onTextChannelDelete(TextChannelDeleteEvent event) {
		executor.execute(() -> {
			try {
				ScheduledCommandDAO.getInstance().removeAllScheduledInChannel(event.getChannel().getId());
				RssDAO.getInstance().removeAllSubsInChannel(event.getChannel().getId());
				channelDAO.removeTextChannel(event.getChannel());
			} catch (SQLException e) {
				LOGGER.warning("Ran into error when removing text channel from db", e);
			}
		});
	}

	@Override
	public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
		executor.execute(() -> channelDAO.removeVoiceChannel(event.getChannel()));
	}

	@Override
	public void onTextChannelUpdateName(TextChannelUpdateNameEvent event) {
		executor.execute(() -> channelDAO.addTextChannel(event.getChannel()));
	}

	@Override
	public void onVoiceChannelUpdateName(VoiceChannelUpdateNameEvent event) {
		executor.execute(() -> channelDAO.addVoiceChannel(event.getChannel()));
	}

	private void checkVoiceLobby(GuildVoiceUpdateEvent event) {
		Guild guild = event.getGuild();
		GuildVoiceConnection conn = GuildVoiceProvider.Companion.getInstance().getGuildVoiceConnection(guild.getIdLong());
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
			conn.setCurrentVoiceChannel(event.getChannelJoined());
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
