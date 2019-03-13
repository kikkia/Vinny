package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.commands.VoiceCommand;
import com.bot.db.PlaylistDAO;
import com.bot.utils.CommandPermissions;
import com.bot.voice.QueuedAudioTrack;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class SaveMyPlaylistCommand extends VoiceCommand {
	private static final Logger LOGGER = Logger.getLogger(SaveMyPlaylistCommand.class.getName());

	private PlaylistDAO playlistDAO;
	private Bot bot;

	public SaveMyPlaylistCommand(Bot bot) {
		this.bot = bot;
		this.name = "savemyplaylist";
		this.arguments = "Name";
		this.help = "Saves the current audio playlist as a playlist accessible for any server you are on.";

		playlistDAO = PlaylistDAO.getInstance();
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
		// Check the permissions to do the command
		if (!CommandPermissions.canExecuteCommand(this, commandEvent))
			return;

		String args = commandEvent.getArgs();
		if (args.equals("")) {
			commandEvent.reply("You need to specify a name for the playlist.");
			return;
		}

		LinkedList<QueuedAudioTrack> tracks = new LinkedList<>(bot.getHandler(commandEvent.getGuild()).getTracks());
		QueuedAudioTrack nowPlaying = bot.getHandler(commandEvent.getGuild()).getNowPlaying();

		List<QueuedAudioTrack> trackList = new LinkedList<>();
		trackList.add(nowPlaying);
		trackList.addAll(tracks);

		if (playlistDAO.createPlaylistForUser(commandEvent.getAuthor().getId(), args, trackList)) {
			commandEvent.reply("Playlist successfully created.");
		} else {
			commandEvent.reply("Something went wrong! Failed to create playlist.");
		}
	}
}
