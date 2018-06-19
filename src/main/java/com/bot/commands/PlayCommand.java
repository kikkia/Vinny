package com.bot.commands;

import com.bot.Bot;
import com.bot.voice.QueuedAudioTrack;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import java.util.logging.Logger;

public class PlayCommand extends Command {
	private static final Logger LOGGER = Logger.getLogger(PlayCommand.class.getName());

	private Bot bot;

	public PlayCommand(Bot bot) {
		this.bot = bot;
		this.name = "play";
		this.arguments = "<title|URL>";
		this.help = "plays the provided audio track";
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		if (commandEvent.getArgs().isEmpty()) {
			VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
			
			if(handler != null && handler.getPlayer().isPaused()) {
				handler.getPlayer().setPaused(false);
				commandEvent.reply(commandEvent.getClient().getSuccess() + " Resumed paused stream.");
			}
			else {
				commandEvent.reply(commandEvent.getClient().getWarning() + " You must give me something to play.\n" +
						"`" + commandEvent.getClient().getPrefix() +"play <URL>` - Plays media at the provided URL\n" +
						"`" + commandEvent.getClient().getPrefix() + "play <search term>` - Searchs youtube for the first result of the search term");

			}
			return;
		}

		String URLRegex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

		if (commandEvent.getArgs().matches(URLRegex)) {
			// URL input, grab from source
			// Separate URL from any other possible args
			String URL = commandEvent.getArgs().split(" ")[0];
			commandEvent.reply("\u231A Loading... `["+commandEvent.getArgs()+"]`", m -> bot.getManager().loadItemOrdered(commandEvent.getGuild(), URL, new PlayHandler(m, URL, commandEvent, false)));
		}
		else {
			// Not a URL, Treat as a YT search
			commandEvent.reply("\u231A Searching for `["+commandEvent.getArgs()+"]`", m -> bot.getManager().loadItemOrdered(commandEvent.getGuild(), "ytsearch:" + commandEvent.getArgs(), new PlayHandler(m, commandEvent.getArgs(), commandEvent, true)));
		}
	}

	private class PlayHandler implements AudioLoadResultHandler {
		private final CommandEvent commandEvent;
		private final String source;
		private final Message message;
		private final boolean search;

		private PlayHandler(Message message, String source, CommandEvent commandEvent, boolean search) {
			this.message = message;
			this.source = source;
			this.commandEvent = commandEvent;
			this.search = search;
		}

		private void loadTracks(AudioTrack track, AudioPlaylist playlist) {
			if (VoiceSendHandler.isSongTooLong(track)) {
				message.editMessage(commandEvent.getClient().getWarning() + " The track was longer than the max length of " +
						QueuedAudioTrack.msToMinSec(VoiceSendHandler.MAX_DURATION * 1000)).queue();
				return;
			}
			// If the queue track was successfull go on, if not return.
			if (bot.queueTrack(track, commandEvent, message) && (playlist == null || !commandEvent.getSelfMember().hasPermission(commandEvent.getTextChannel(), Permission.MESSAGE_ADD_REACTION))) {
				message.editMessage(commandEvent.getClient().getSuccess() + " Successfully added track to queue.").queue();
			}
		}

		@Override
		public void trackLoaded(AudioTrack audioTrack) {
			loadTracks(audioTrack, null);
		}

		@Override
		public void playlistLoaded(AudioPlaylist audioPlaylist) {
			if (audioPlaylist.isSearchResult() || audioPlaylist.getTracks().size() == 0) {
				loadTracks(audioPlaylist.getTracks().get(0), null);
			}
			else if (audioPlaylist.getSelectedTrack() != null) {
				loadTracks(audioPlaylist.getSelectedTrack(), null);
			}
			else {
				loadTracks(audioPlaylist.getTracks().get(0), null);
				// TODO: Playlists
			}
 		}

		@Override
		public void noMatches() {
			message.editMessage(commandEvent.getClient().getWarning() + " Failed to find a track for " + commandEvent.getArgs()).queue();
		}

		@Override
		public void loadFailed(FriendlyException e) {
			// If a common exception give the message
			if (e.severity == FriendlyException.Severity.COMMON) {
				message.editMessage(commandEvent.getClient().getError() + " I encountered an error loading track: \n `" +
						e.getMessage() + "`").queue();
			}
			// If an uncommon exception do not give any details to the user
			else {
				message.editMessage(commandEvent.getClient().getError() + " I encountered an error loading track.").queue();
			}
		}
	}
}
