package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.db.PlaylistDAO;
import com.bot.models.Playlist;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

public class ListGuildPlaylistCommand extends VoiceCommand {

    private PlaylistDAO playlistDAO;

    public ListGuildPlaylistCommand() {
        this.playlistDAO = PlaylistDAO.getInstance();
        this.name = "gplaylists";
        this.help = "Returns all of the playlists the guild has.";
    }

    @Override
    //@trace(operationName = "executeCommand", resourceName = "guildPlaylists")
    protected void executeCommand(CommandEvent commandEvent) {
        List<Playlist> playlistList = playlistDAO.getPlaylistsForGuild(commandEvent.getGuild().getId());

        if (playlistList.size() == 0) {
            commandEvent.reply("Your guild doesn't have any playlists. :cry:");
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
