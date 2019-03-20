package com.bot;

import com.bot.db.ChannelDAO;
import com.bot.db.GuildDAO;
import com.bot.db.MembershipDAO;
import com.bot.models.InternalGuild;
import com.bot.utils.Config;
import com.bot.utils.HttpUtils;
import com.bot.utils.Logger;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.channel.text.GenericTextChannelEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.core.events.channel.voice.GenericVoiceChannelEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.logging.Level;

public class Bot extends ListenerAdapter {
	private static final Logger LOGGER = new Logger(Bot.class.getName());
	private EventWaiter waiter;
	private final AudioPlayerManager manager;

	private Config config;

	private GuildDAO guildDAO;
	private MembershipDAO membershipDAO;
	private ChannelDAO channelDAO;

	public final static String SUPPORT_INVITE_LINK = "https://discord.gg/XMwyzxZ";


	Bot(EventWaiter waiter) {
		this.config = Config.getInstance();
		this.waiter = waiter;
		this.manager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(manager);

		guildDAO = GuildDAO.getInstance();
		membershipDAO = MembershipDAO.getInstance();
		channelDAO = ChannelDAO.getInstance();
	}

	// This code runs every time a message is received by the bot
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
//		String[] command = event.getMessage().getContentRaw().split(" ", 2);
//
//		if ("hi".equals(command[0])){
//			event.getTextChannel().sendMessage("yo").queue();
//			waiter.waitForEvent(MessageReceivedEvent.class, getResponseFromSender(event), responseConsumer(event), 10, TimeUnit.SECONDS, new exampleTimeout(event));
//		}
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		checkVoiceLobby(event);
	}

	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		checkVoiceLobby(event);
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		if (!addGuildIfNotPresent(event)) {
			LOGGER.log(Level.SEVERE, "Failed to add guild to db, dont add membership.");
			return;
		}
		membershipDAO.addUserToGuild(event.getUser(), event.getGuild());
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		membershipDAO.removeUserMembershipToGuild(event.getUser().getId(), event.getGuild().getId());
	}

	@Override
	public void onGuildJoin(GuildJoinEvent guildJoinEvent) {
		guildDAO.addFreshGuild(guildJoinEvent.getGuild());

		// If we are posting stats to external discord bot sites, then do it
		if (Boolean.parseBoolean(config.getConfig(Config.ENABLE_EXTERNAL_APIS)))
			HttpUtils.postGuildCountToExternalSites();
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent guildLeaveEvent) {
		for (Member m : guildLeaveEvent.getGuild().getMembers()) {
			membershipDAO.removeUserMembershipToGuild(m.getUser().getId(), guildLeaveEvent.getGuild().getId());
		}

		// If we are posting stats to external discord bot sites, then do it
		if (Boolean.parseBoolean(config.getConfig(Config.ENABLE_EXTERNAL_APIS)))
			HttpUtils.postGuildCountToExternalSites();
	}


	@Override
	public void onTextChannelCreate(TextChannelCreateEvent event) {
		if (!addGuildIfNotPresent(event)) {
			LOGGER.log(Level.SEVERE, "Failed to add guild to db, dont add text channel.");
			return;
		}
		channelDAO.addTextChannel(event.getChannel());
	}

	@Override
	public void onVoiceChannelCreate(VoiceChannelCreateEvent event) {
		if (!addGuildIfNotPresent(event)) {
			LOGGER.log(Level.SEVERE, "Failed to add guild to db, dont add voice channel.");
			return;
		}
		channelDAO.addVoiceChannel(event.getChannel());
	}

	@Override
	public void onTextChannelDelete(TextChannelDeleteEvent event) {
		channelDAO.removeTextChannel(event.getChannel());
	}

	@Override
	public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
		channelDAO.removeVoiceChannel(event.getChannel());
	}

	@Override
	public void onTextChannelUpdateName(TextChannelUpdateNameEvent event) {
		// This should trip the on duplicate sync the names
		channelDAO.addTextChannel(event.getChannel());
	}

	@Override
	public void onVoiceChannelUpdateName(VoiceChannelUpdateNameEvent event) {
		// This should trip the on duplicate sync the names
		channelDAO.addVoiceChannel(event.getChannel());
	}

//
//	// Predicate defines the condition we need to fulfill
//	private Predicate<MessageReceivedEvent> getResponseFromSender(MessageReceivedEvent original) {
//		return p -> p.getAuthor() == original.getAuthor() && p.getMessage().getContentRaw().equals("sup");
//	}
//
//	// This code will run when the predicate is met
//	private Consumer<MessageReceivedEvent> responseConsumer(MessageReceivedEvent event) {
//		return x -> event.getTextChannel().sendMessage("not much fam hbu").queue();
//	}
//
//	// Runnable to execute when timeout is met.
//	public class exampleTimeout implements Runnable {
//		TextChannel channel;
//
//		public exampleTimeout(MessageReceivedEvent event) {
//			channel = event.getTextChannel();
//		}
//
//		@Override
//		public void run() {
//			channel.sendMessage("oops out of time!").queue();
//		}
//	}


	public AudioPlayerManager getManager() {
		return manager;
	}

	public boolean queueTrack(AudioTrack track, CommandEvent event, Message m) {
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
			getHandler(event.getGuild()).queueTrack(track, event.getAuthor().getIdLong());
			if (!event.getGuild().getAudioManager().isConnected()) {
				event.getGuild().getAudioManager().openAudioConnection(event.getMember().getVoiceState().getChannel());
				// Add the stream to the shard tracking info
				ShardingManager.getInstance().getShards().get(event.getJDA().getShardInfo().getShardId()).addVoiceStream();
			}
			return true;
		}
	}

	public VoiceSendHandler getHandler(Guild guild) {
		VoiceSendHandler handler;
		if (guild.getAudioManager().getSendingHandler() == null) {
			AudioPlayer player = manager.createPlayer();
			handler = new VoiceSendHandler(guild.getIdLong(), player, this);

			// Get default volume
			int dVolume = 100;
			InternalGuild g = guildDAO.getGuildById(guild.getId());

			if (g == null) {
				LOGGER.warning("Failed to get guild when looking for volume. Attempting an add");
				guildDAO.addFreshGuild(guild);
				// Just play, no need to return
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
		Guild guild = event.getGuild();
		VoiceSendHandler handler = getHandler(guild);
		AudioManager manager = guild.getAudioManager();
		if (manager.isConnected() && manager.getConnectedChannel().getMembers().size() == 1) {
			handler.stop();
			manager.closeAudioConnection();
			// Remove voice stream from shard tracking
			ShardingManager.getInstance().getShards().get(event.getJDA().getShardInfo().getShardId()).removeVoiceStream();
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
