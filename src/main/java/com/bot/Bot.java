package com.bot;

import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.jagrosh.jdautilities.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Bot extends ListenerAdapter {
	private EventWaiter waiter;
	private JDA jda;
	private final AudioPlayerManager manager;
	public final static Command.Category VOICE = new Command.Category("Voice");
	public final static Command.Category MEME = new Command.Category("Meme");
	public final static Command.Category NSFW = new Command.Category("Nsfw");
	public final static Command.Category MOD = new Command.Category("MODERATION");


	Bot(EventWaiter waiter) {
		this.waiter = waiter;
		this.manager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(manager);
	}

	// This code runs every time a message is received by the bot
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String[] command = event.getMessage().getContentRaw().split(" ", 2);
		Guild guild = event.getGuild();

		if ("hi".equals(command[0])){
			event.getTextChannel().sendMessage("yo").queue();
			waiter.waitForEvent(MessageReceivedEvent.class, getResponseFromSender(event), responseConsumer(event), 10, TimeUnit.SECONDS, new exampleTimeout(event));
		}
	}

	// Predicate defines the condition we need to fulfill
	private Predicate<MessageReceivedEvent> getResponseFromSender(MessageReceivedEvent original) {
		System.out.println("here");
		return p -> p.getAuthor() == original.getAuthor() && p.getMessage().getContentRaw().equals("sup");
	}

	// This code will run when the predicate is met
	private Consumer<MessageReceivedEvent> responseConsumer(MessageReceivedEvent event) {
		return x -> event.getTextChannel().sendMessage("not much fam hbu").queue();
	}

	// Runnable to execute when timeout is met.
	public class exampleTimeout implements Runnable {
		TextChannel channel;

		public exampleTimeout(MessageReceivedEvent event) {
			channel = event.getTextChannel();
		}

		@Override
		public void run() {
			channel.sendMessage("oops out of time!").queue();
		}
	}

	public JDA getJda() {
		return jda;
	}

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
			}
			return true;
		}
	}

	public VoiceSendHandler getHandler(Guild guild) {
		VoiceSendHandler handler;
		if (guild.getAudioManager().getSendingHandler() == null) {
			AudioPlayer player = manager.createPlayer();
			// TODO: Add Default Volume from DB
			handler = new VoiceSendHandler(guild.getIdLong(), player, this);
			player.addListener(handler);
			guild.getAudioManager().setSendingHandler(handler);
		}
		else {
			handler = (VoiceSendHandler) guild.getAudioManager().getSendingHandler();
		}
		return handler;
	}

}
