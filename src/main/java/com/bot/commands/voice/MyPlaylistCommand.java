package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.db.PlaylistRepository;
import com.bot.models.Playlist;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;

import java.util.List;
import java.util.logging.Logger;

public class MyPlaylistCommand extends Command {
    private static final Logger LOGGER = Logger.getLogger(MyPlaylistCommand.class.getName());

    private Bot bot;
	private PlaylistRepository playlistRepository;

	public MyPlaylistCommand(Bot bot) {
		this.bot = bot;
		this.playlistRepository = PlaylistRepository.getInstance();
		this.name = "myplaylists";
		this.help = "Returns all of the playlists you have";
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		List<Playlist> playlistList = playlistRepository.getPlaylistsForUser(commandEvent.getAuthor().getId());
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