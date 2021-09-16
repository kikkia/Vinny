package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.db.PlaylistDAO;
import com.bot.models.Playlist;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListMyPlaylistCommand extends VoiceCommand {

	private PlaylistDAO playlistDAO;

	public ListMyPlaylistCommand(PlaylistDAO playlistDAO) {
		this.playlistDAO = playlistDAO;
		this.name = "myplaylists";
		this.help = "Returns all of the playlists you have";
	}

	@Override
	@Trace(operationName = "executeCommand", resourceName = "myPlaylists")
	protected void executeCommand(CommandEvent commandEvent) {
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
