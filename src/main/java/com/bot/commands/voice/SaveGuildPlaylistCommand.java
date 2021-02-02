package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.commands.VoiceCommand;
import com.bot.db.PlaylistDAO;
import com.bot.voice.QueuedAudioTrack;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;

import java.util.LinkedList;
import java.util.List;

public class SaveGuildPlaylistCommand extends VoiceCommand {
	private PlaylistDAO playlistDAO;
	private Bot bot;

	public SaveGuildPlaylistCommand(Bot bot) {
		this.name = "savegplaylist";
		this.arguments = "<playlist name>";
		this.help = "Saves all of the currently queued tracks into a playlist tied to your guild.";

		this.playlistDAO = PlaylistDAO.getInstance();
		this.bot = bot;
	}

	@Override
	@Trace(operationName = "executeCommand", resourceName = "SaveGuildPlaylist")
	protected void executeCommand(CommandEvent commandEvent) {
		String args = commandEvent.getArgs();
		if (args.isEmpty()) {
			commandEvent.reply("You need to specify a name for the playlist.");
			return;
		}

		LinkedList<QueuedAudioTrack> tracks = new LinkedList<>(bot.getHandler(commandEvent.getGuild()).getTracks());
		QueuedAudioTrack nowPlaying = bot.getHandler(commandEvent.getGuild()).getNowPlaying();

		List<QueuedAudioTrack> trackList = new LinkedList<>();
		trackList.add(nowPlaying);
		trackList.addAll(tracks);

		if (playlistDAO.createPlaylistForGuild(commandEvent.getGuild().getId(), args, trackList)) {
			commandEvent.reply("Playlist successfully created.");
		} else {
			commandEvent.reply("Something went wrong! Failed to create playlist.");
			metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
		}
	}
}
