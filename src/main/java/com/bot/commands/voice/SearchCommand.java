package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.commands.VoiceCommand;
import com.bot.exceptions.MaxQueueSizeException;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.TimeUnit;

import static com.bot.utils.FormattingUtils.msToMinSec;

public class SearchCommand extends VoiceCommand {
	private Bot bot;
	private final OrderedMenu.Builder builder;

	public SearchCommand(Bot bot, EventWaiter eventWaiter) {
		this.name = "search";
		this.arguments = "<Search terms>";
		this.help = "Searches youtube and replies with a list of tracks to play.";

		this.builder = new OrderedMenu.Builder()
				.useNumbers()
				.allowTextInput(false)
				.useCancelButton(true)
				.setEventWaiter(eventWaiter)
				.setTimeout(30, TimeUnit.SECONDS);

		this.bot = bot;
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
				commandEvent.reply(commandEvent.getClient().getWarning() + " You must give me something to search for.");
			}
			return;
		}

		commandEvent.reply("Searching for `[" + commandEvent.getArgs() + "]`",
				m -> bot.getManager().loadItemOrdered(commandEvent.getGuild(), "ytsearch:" + commandEvent.getArgs(), new PlayHandler(m, commandEvent)));
	}

	private class PlayHandler implements AudioLoadResultHandler {
		private final CommandEvent commandEvent;
		private final Message message;

		private PlayHandler(Message message, CommandEvent commandEvent) {
			this.message = message;
			this.commandEvent = commandEvent;
		}

		private void loadTracks(AudioTrack track) {
			if (VoiceSendHandler.isSongTooLong(track)) {
				message.editMessage(commandEvent.getClient().getWarning() + " The track was longer than the max length of " +
						msToMinSec(VoiceSendHandler.MAX_DURATION * 1000)).queue();
				return;
			}
			// If the queue track was successful go on, if not return.
			try {
				if (bot.queueTrack(track, commandEvent, message)) {
					message.editMessage(commandEvent.getClient().getSuccess() + " Successfully added `"+ track.getInfo().title + "` to queue.").queue();
				} else {
					message.editMessage(commandEvent.getClient().getError() + " Failed to add track to playlist.").queue();
				}
			} catch (MaxQueueSizeException e) {
				message.editMessage(e.getMessage()).queue();
			}
		}

		@Override
		public void trackLoaded(AudioTrack audioTrack) {
			loadTracks(audioTrack);
		}

		@Override
		public void playlistLoaded(AudioPlaylist audioPlaylist) {
			builder.setCancel(message1 -> {})
					.setChoices(new String[0])
					.setUsers(commandEvent.getAuthor())
					.setColor(commandEvent.getSelfMember().getColor())
					.setText(commandEvent.getClient().getSuccess() + " Results from search:")
					.setSelection((message, i) -> {
						AudioTrack track = audioPlaylist.getTracks().get(i-1);

						if(VoiceSendHandler.isSongTooLong(track)) {
							commandEvent.replyWarning("The selected track is longer than the max allowed length of " +
									msToMinSec(VoiceSendHandler.MAX_DURATION * 1000));
							return;
						}

						try {
							bot.queueTrack(track, commandEvent, message);
						} catch (MaxQueueSizeException e) {
							commandEvent.replyWarning(e.getMessage());
							return;
						}
						commandEvent.replySuccess("Added `" + track.getInfo().title + "` to the queue");
					})
					.setUsers(commandEvent.getAuthor())
					.setTimeout(2, TimeUnit.MINUTES);

			for(int i = 0; i < 5 && i < audioPlaylist.getTracks().size(); i++) {
				AudioTrack track = audioPlaylist.getTracks().get(i);
				builder.addChoices("`" + msToMinSec(track.getDuration()) +"` [**" + track.getInfo().title + "**]");
			}

			builder.build().display(message);
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
				logger.severe("Failed to load a track", e);
			}
			// If an uncommon exception do not give any details to the user
			else {
				logger.severe("Failed to load a track", e);
				message.editMessage(commandEvent.getClient().getError() + " I encountered an error loading track.").queue();
			}
		}
	}
}
