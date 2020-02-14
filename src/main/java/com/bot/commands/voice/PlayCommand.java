package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.commands.VoiceCommand;
import com.bot.exceptions.MaxQueueSizeException;
import com.bot.utils.Config;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;

import static com.bot.utils.FormattingUtils.msToMinSec;

public class PlayCommand extends VoiceCommand {

	private Bot bot;

	public PlayCommand(Bot bot) {
		this.bot = bot;
		this.name = "play";
		this.arguments = "<title|URL>";
		this.help = "plays the provided audio track";
	}

	@Override
	protected void executeCommand(CommandEvent commandEvent) {
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
		String URL = commandEvent.getArgs().split(" ")[0];

		if (URL.matches(URLRegex)) {
			// URL input, grab from source
			// Separate URL from any other possible args
			commandEvent.reply("\u231A Loading... `["+commandEvent.getArgs()+"]`", m -> bot.getManager().loadItemOrdered(commandEvent.getGuild(), URL, new PlayHandler(m, URL, commandEvent, false)));
		}
		else {
			// Not a URL, Treat as a search
			String searchPrefix = Config.getInstance().getConfig(Config.DEFAULT_SEARCH_PROVIDER, "ytsearch:");
			commandEvent.reply("\u231A Searching for `["+commandEvent.getArgs()+"]`", m -> bot.getManager().loadItemOrdered(commandEvent.getGuild(),  searchPrefix + commandEvent.getArgs(), new PlayHandler(m, commandEvent.getArgs(), commandEvent, true)));
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

		private boolean loadTracks(AudioTrack track, List<AudioTrack> playlist, boolean fromList) throws MaxQueueSizeException {
			if (playlist == null) {
				if (VoiceSendHandler.isSongTooLong(track)) {
					message.editMessage(commandEvent.getClient().getWarning() + " The track was longer than the max length of " +
							msToMinSec(VoiceSendHandler.MAX_DURATION * 1000)).queue();
					return false;
				}
				// If the queue track was successful go on, if not return.
				if (bot.queueTrack(track, commandEvent, message)) {
					if (!fromList)
						message.editMessage(commandEvent.getClient().getSuccess() + " Successfully added `"+ track.getInfo().title + "` to queue.").queue();

					return true;
				} else {
					message.editMessage(commandEvent.getClient().getError() + " Failed to add track to playlist.").queue();
					return false;
				}
			} else {
				// This will only happen with a playlist
				int count = 0;
				List<String> tracksAdded = new ArrayList<>();
				for (AudioTrack t : playlist) {
					if (loadTracks(t, null, true)) {
						count++;
						tracksAdded.add(t.getInfo().title);
					} else {
						message.editMessage(commandEvent.getClient().getError() + " Failed to add a track to playlist. Added " + count + " tracks.").queue();
						return false;
					}
				}
				if (!tracksAdded.isEmpty()) {
					commandEvent.reply("Added the following tracks: " + prettyPrintTracks(tracksAdded));
				}
				return true;
			}
		}

		@Override
		public void trackLoaded(AudioTrack audioTrack) {
			try {
				loadTracks(audioTrack, null, false);
			} catch (MaxQueueSizeException e) {
				message.editMessage(e.getMessage()).queue();
			}
		}

		@Override
		public void playlistLoaded(AudioPlaylist audioPlaylist) {
			if (audioPlaylist.isSearchResult()) {
				AudioTrack track = audioPlaylist.getTracks().get(0);
				try {
					loadTracks(track, null, false);
				} catch (MaxQueueSizeException e) {
					message.editMessage(e.getMessage()).queue();
				}
				return;
			}
			// They gave multiple args, assume one is the tracks.
			String[] trackNums = {};
			if (commandEvent.getArgs().split(" ").length == 2)
				trackNums = commandEvent.getArgs().split(" ")[1].split("-");

			if(trackNums.length == 2) {
				int to, from;
				try {
					from = Integer.parseInt(trackNums[0]) - 1; // Account for zero index
					to = Integer.parseInt(trackNums[1]);
				} catch (NumberFormatException e) {
					commandEvent.reply(commandEvent.getClient().getWarning() + "  NumberFormatException: Invalid number given, please only user numeric characters.");
					return;
				}

				if (from > to) {
					commandEvent.reply(commandEvent.getClient().getWarning() + " Error: Beginning index is bigger than ending index.");
					return;
				} else if (from < 0) {
					commandEvent.reply(commandEvent.getClient().getWarning() + " Error: Beginning index is less than 1.");
					return;
				} else if (to > audioPlaylist.getTracks().size()) {
					commandEvent.reply(commandEvent.getClient().getWarning() + " Error: Requesting tracks out of range. Only " + audioPlaylist.getTracks().size() + " tracks in playlist :x:");
					return;
				} else if (to - from > 9) {
					commandEvent.reply(commandEvent.getClient().getWarning() + " Warning: Requesting number of tracks that is greater than 10. Trimming results to " +
							"" + from + "-" + (from + 9) +" :exclamation:");
					to = from + 9;
				}
				try {
					loadTracks(null, audioPlaylist.getTracks().subList(from, to), true);
				} catch (MaxQueueSizeException e) {
					message.editMessage(e.getMessage()).queue();
				}
			} else {
				try {
					loadTracks(null, audioPlaylist.getTracks(), true);
				} catch (MaxQueueSizeException e) {
					message.editMessage(e.getMessage()).queue();
				}
			}
 		}

		@Override
		public void noMatches() {
			message.editMessage(commandEvent.getClient().getWarning() + " Failed to find a track for " + commandEvent.getArgs()).queue();
		}

		@Override
		public void loadFailed(FriendlyException e) {
			message.editMessage(commandEvent.getClient().getError() + " I encountered an error loading track: " + e.getMessage()).queue();
			logger.severe("Failed to load a track", e);
		}

		private String prettyPrintTracks(List<String> tracks) {
			String prettyString = "```";
			for (String s: tracks) {
				prettyString += s + "\n";
			}
			prettyString += "```";
			return prettyString;
		}
	}
}
