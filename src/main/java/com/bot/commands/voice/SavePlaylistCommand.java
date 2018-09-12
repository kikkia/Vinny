package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.db.PlaylistRepository;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.logging.Logger;

public class SavePlaylistCommand extends Command {
	private static final Logger LOGGER = Logger.getLogger(SavePlaylistCommand.class.getName());
	private PlaylistRepository playlistRepository;
	private Bot bot;

	public SavePlaylistCommand(Bot bot) {
		this.name = "saveguildplaylist";
		this.arguments = "<playlist name>";
		this.help = "Saves all of the currently queued tracks into a playlist tied to your guild.";
		this.playlistRepository = PlaylistRepository.getInstance();
		this.bot = bot;
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		// TODO: Take current playlist/range and save it to DB
	}
}
