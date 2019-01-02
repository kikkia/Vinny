package com.bot.commands.voice;

import com.bot.db.PlaylistDAO;
import com.bot.models.Playlist;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;

import java.util.List;
import java.util.logging.Logger;

public class ListMyPlaylistCommand extends Command {
    private static final Logger LOGGER = Logger.getLogger(ListMyPlaylistCommand.class.getName());

	private PlaylistDAO playlistDAO;

	public ListMyPlaylistCommand() {
		this.playlistDAO = PlaylistDAO.getInstance();
		this.name = "myplaylists";
		this.help = "Returns all of the playlists you have";
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		List<Playlist> playlistList = playlistDAO.getPlaylistsForUser(commandEvent.getAuthor().getId());

		if (playlistList.size() == 0) {
			commandEvent.reply("You don't have any playlists. :cry:");
			return;
		}

		StringBuilder reply = new StringBuilder();
		for (Playlist p: playlistList) {
			reply.append(p.getId()).append(": ");
			reply.append(p.getName()).append(" | ");
			reply.append("Total tracks: ").append(p.getTracks().size());
			reply.append("\n");
		}
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("Playlists", reply.toString(), false);
		commandEvent.reply(embedBuilder.build());
	}
}
