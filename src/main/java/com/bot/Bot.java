package com.bot;

import com.bot.db.*;
import com.bot.exceptions.MaxQueueSizeException;
import com.bot.metrics.MetricsManager;
import com.bot.models.InternalGuild;
import com.bot.models.InternalShard;
import com.bot.tasks.AddFreshGuildDeferredTask;
import com.bot.tasks.LeaveGuildDeferredTask;
import com.bot.utils.*;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.impl.CommandClientImpl;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.channel.text.GenericTextChannelEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.voice.GenericVoiceChannelEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public class Bot extends ListenerAdapter {
	private final Logger LOGGER;
	private final AudioPlayerManager manager;

	private Config config;

	private GuildDAO guildDAO;
	private MembershipDAO membershipDAO;
	private ChannelDAO channelDAO;
	private MetricsManager metricsManager;
	private ScheduledExecutorService executor;

	public final static String SUPPORT_INVITE_LINK = "https://discord.gg/XMwyzxZ";


	Bot() {
		this.config = Config.getInstance();
		this.manager = new DefaultAudioPlayerManager();

		YoutubeAudioSourceManager ytSource = new YoutubeAudioSourceManager(true);

		manager.registerSourceManager(ytSource);

		AbstractRoutePlanner planner = LavaPlayerUtils.getIPRoutePlanner();
		if (planner != null) {
			YoutubeIpRotatorSetup setup = new YoutubeIpRotatorSetup(planner);
			setup.forSource(ytSource)
					.forManager(manager);
			setup.setup();
		}

		manager.registerSourceManager(new SoundCloudAudioSourceManager.Builder().withAllowSearch(true).build());
		manager.registerSourceManager(new BandcampAudioSourceManager());
		manager.registerSourceManager(new VimeoAudioSourceManager());
		manager.registerSourceManager(new TwitchStreamAudioSourceManager());
		manager.registerSourceManager(new BeamAudioSourceManager());
		manager.registerSourceManager(new HttpAudioSourceManager());

		guildDAO = GuildDAO.getInstance();
		membershipDAO = MembershipDAO.getInstance();
		channelDAO = ChannelDAO.getInstance();

		LOGGER =  new Logger(Bot.class.getName());
		metricsManager = MetricsManager.getInstance();
		executor = Executors.newScheduledThreadPool(15);
	}

	@Override
	public void onReady(ReadyEvent event) {
		ShardingManager shardingManager = ShardingManager.getInstance();
		shardingManager.putShard(new InternalShard(event.getJDA()));
		System.out.println("Shard: " + event.getJDA().getShardInfo().getShardId() + " ready");

		if (Boolean.parseBoolean(config.getConfig(Config.DATA_LOADER))) {
			DataLoader.LoadThread t = null;
			try {
				t = new DataLoader.LoadThread(event.getJDA(), System.currentTimeMillis());
				t.start();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		super.onReady(event);
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		executor.execute(() -> {
			InternalGuild guild = guildDAO.getGuildById(event.getGuild().getId());
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
		});
		super.onGuildMessageReceived(event);
	}

	@Override
	public void onGenericEvent(@NotNull GenericEvent event) {
		metricsManager.markDiscordEvent(event.getJDA().getShardInfo().getShardId(), event.getClass().getSimpleName());
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
		executor.execute(() -> {
			if (!addGuildIfNotPresent(event)) {
				LOGGER.log(Level.SEVERE, "Failed to add guild to db, dont add membership.");
				return;
			}
			membershipDAO.addUserToGuild(event.getUser(), event.getGuild());
		});
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
	public void onTextChannelCreate(TextChannelCreateEvent event) {
		executor.execute(() -> {
			if (!addGuildIfNotPresent(event)) {
				LOGGER.log(Level.SEVERE, "Failed to add guild to db, dont add text channel.");
				return;
			}
			channelDAO.addTextChannel(event.getChannel());
		});
	}

	@Override
	public void onVoiceChannelCreate(VoiceChannelCreateEvent event) {
		executor.execute(() -> {
			if (!addGuildIfNotPresent(event)) {
				LOGGER.log(Level.SEVERE, "Failed to add guild to db, dont add voice channel.");
				return;
			}
			channelDAO.addVoiceChannel(event.getChannel());
		});
	}

	@Override
	public void onTextChannelDelete(TextChannelDeleteEvent event) {
		executor.execute(() -> {
			try {
				ScheduledCommandDAO.getInstance().removeAllScheduledInChannel(event.getChannel().getId());
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

	public AudioPlayerManager getManager() {
		return manager;
	}

	// TODO: Move this audio handling stuff out of the bot class
	public boolean queueTrack(AudioTrack track, CommandEvent event, Message m) throws MaxQueueSizeException {
		if (event.getMember().getVoiceState().getChannel() == null) {
			m.editMessage(event.getClient().getWarning() + " You are not in a voice channel! Please join one to use this command.").queue();
			return false;
		}
		else if (!event.getSelfMember().hasPermission(event.getMember().getVoiceState().getChannel(), Permission.VOICE_CONNECT)) {
			m.editMessage(event.getClient().getWarning() + " I don't have permission to join your voice channel. :cry:").queue();
			return false;
		}
		else if (!event.getSelfMember().hasPermission(event.getMember().getVoiceState().getChannel(), Permission.VOICE_SPEAK)){
			m.editMessage(event.getClient().getWarning() + " I don't have permission to speak in your voice channel. :cry:").queue();
			return false;
		}
		else {
			getHandler(event.getGuild()).queueTrack(track, event.getAuthor().getIdLong(), event.getAuthor().getName(), event.getTextChannel());
			if (!event.getGuild().getAudioManager().isConnected()) {
				event.getGuild().getAudioManager().openAudioConnection(event.getMember().getVoiceState().getChannel());
			}
			return true;
		}
	}

	public VoiceSendHandler getHandler(Guild guild) {
		VoiceSendHandler handler;
		if (guild.getAudioManager().getSendingHandler() == null) {
			AudioPlayer player = manager.createPlayer();
			handler = new VoiceSendHandler(player);

			// Get default volume
			int dVolume = 100;
			InternalGuild g = guildDAO.getGuildById(guild.getId());

			if (g == null) {
				LOGGER.warning("Failed to get guild when looking for volume. Attempting an add");
				guildDAO.addFreshGuild(guild);
				// Just play, no need to return
			} else {
				dVolume = g.getVolume();
			}

			handler.getPlayer().setVolume(dVolume);
			player.addListener(handler);
			guild.getAudioManager().setSendingHandler(handler);
		}
		else {
			handler = (VoiceSendHandler) guild.getAudioManager().getSendingHandler();
		}
		return handler;
	}

	private void checkVoiceLobby(GuildVoiceUpdateEvent event) {
		Guild guild = event.getEntity().getGuild();
		VoiceSendHandler handler = getHandler(guild);
		AudioManager manager = guild.getAudioManager();

		// if there are no humans left, then leave
		int users = 0;
		for (Member member : manager.getConnectedChannel().getMembers()) {
			if (!member.getUser().isBot())
				users++;
		}

		if (manager.isConnected() && users < 1) {
			handler.stop();
			manager.closeAudioConnection();
		}
	}

	private boolean addGuildIfNotPresent(GenericGuildEvent event) {
		InternalGuild guild = guildDAO.getGuildById(event.getGuild().getId());

		if (guild == null) {
			LOGGER.log(Level.SEVERE, "Guild not in DB when adding membership, adding. Guild " + event.getGuild().getId());
			return guildDAO.addGuild(event.getGuild());
		}
		return true;
	}

	private boolean addGuildIfNotPresent(GenericTextChannelEvent event) {
		InternalGuild guild = guildDAO.getGuildById(event.getGuild().getId());

		if (guild == null) {
			LOGGER.log(Level.SEVERE, "Guild not in DB when adding membership, adding. Guild " + event.getGuild().getId());
			return guildDAO.addGuild(event.getGuild());
		}
		return true;
	}

	private boolean addGuildIfNotPresent(GenericVoiceChannelEvent event) {
		InternalGuild guild = guildDAO.getGuildById(event.getGuild().getId());

		if (guild == null) {
			LOGGER.log(Level.SEVERE, "Guild not in DB when adding membership, adding. Guild " + event.getGuild().getId());
			return guildDAO.addGuild(event.getGuild());
		}
		return true;
	}

}
