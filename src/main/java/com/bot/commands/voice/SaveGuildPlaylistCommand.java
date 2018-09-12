package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.db.PlaylistRepository;
import com.bot.voice.QueuedAudioTrack;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class SaveGuildPlaylistCommand extends Command {
	private static final Logger LOGGER = Logger.getLogger(SaveGuildPlaylistCommand.class.getName());
	private PlaylistRepository playlistRepository;
	private Bot bot;

	public SaveGuildPlaylistCommand(Bot bot) {
		this.name = "savegplaylist";
		this.arguments = "<playlist name>";
		this.help = "Saves all of the currently queued tracks into a playlist tied to your guild.";
		this.playlistRepository = PlaylistRepository.getInstance();
		this.bot = bot;
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
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

		if (playlistRepository.createPlaylistForGuild(commandEvent.getGuild().getId(), args, trackList)) {
			commandEvent.reply("Playlist successfully created.");
		} else {
			commandEvent.reply("Something went wrong! Failed to create playlist.");
		}
	}
}
