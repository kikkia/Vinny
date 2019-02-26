package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.utils.CommandCategories;
import com.bot.utils.CommandPermissions;
import com.bot.voice.QueuedAudioTrack;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PlayCommand extends Command {
	private static final Logger LOGGER = Logger.getLogger(PlayCommand.class.getName());

	private Bot bot;

	public PlayCommand(Bot bot) {
		this.bot = bot;
		this.name = "play";
		this.arguments = "<title|URL>";
		this.help = "plays the provided audio track";
		this.category = CommandCategories.VOICE;
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		// Check the permissions to do the command
		if (!CommandPermissions.canExecuteCommand(this, commandEvent))
			return;

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

		private boolean loadTracks(AudioTrack track, List<AudioTrack> playlist) {
			if (playlist == null) {
				if (VoiceSendHandler.isSongTooLong(track)) {
					message.editMessage(commandEvent.getClient().getWarning() + " The track was longer than the max length of " +
							QueuedAudioTrack.msToMinSec(VoiceSendHandler.MAX_DURATION * 1000)).queue();
					return false;
				}
				// If the queue track was successful go on, if not return.
				if (bot.queueTrack(track, commandEvent, message)) {
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
					if (loadTracks(t, null)) {
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
			loadTracks(audioTrack, null);
		}

		@Override
		public void playlistLoaded(AudioPlaylist audioPlaylist) {
			if (audioPlaylist.isSearchResult()) {
				AudioTrack track = audioPlaylist.getTracks().get(0);
				loadTracks(track, null);
				return;
			}
			if (commandEvent.getArgs().split(" ").length < 2) {
				message.editMessage(commandEvent.getClient().getWarning() + " Playlist detected. Please try again but include the songs you want included." +
						" Example: `~play *playlist url* 1-5` This would load songs 1-5 on the playlist. Limited to loading up to 10 songs at a time.").queue();
			} else {
				// They gave multiple args, assume one is the tracks.
				String[] trackNums = commandEvent.getArgs().split(" ")[1].split("-");

				if(trackNums.length == 2) {
					int to, from;
					try {
						from = Integer.parseInt(trackNums[0]);
						to = Integer.parseInt(trackNums[1]);
					} catch (NumberFormatException e) {
						commandEvent.reply(commandEvent.getClient().getWarning() + "  NumberFormatException: Invalid number given, please only user numeric characters.");
						return;
					}

					if (from > to) {
						commandEvent.reply(commandEvent.getClient().getWarning() + " Error: Beginning index is bigger than ending index.");
						return;
					} else if (from < 0) {
						commandEvent.reply(commandEvent.getClient().getWarning() + " Error: Beginning index is less than 0.");
						return;
					} else if (to > audioPlaylist.getTracks().size()) {
						commandEvent.reply(commandEvent.getClient().getWarning() + " Error: Requesting tracks out of range. Only " + audioPlaylist.getTracks().size() + " tracks in playlist :x:");
						return;
					} else if (to - from > 9) {
						commandEvent.reply(commandEvent.getClient().getWarning() + " Warning: Requesting number of tracks that is greater than 10. Trimming results to " +
								"" + from + "-" + (from + 9) +" :exclamation:");
						to = from + 9;
					}
					loadTracks(null, audioPlaylist.getTracks().subList(from, to));
				} else {
					commandEvent.reply(commandEvent.getClient().getWarning() + " Error: Incorrect number of parameters. Make sure that there are no spaces between your track numbers and the dash.");
				}
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
