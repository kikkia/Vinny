package com.bot.commands.voice;

import com.bot.db.PlaylistDAO;
import com.bot.models.Playlist;
import com.bot.utils.CommandCategories;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;

import java.util.List;
import java.util.logging.Logger;

public class ListGuildPlaylistCommand extends Command {
    private static final Logger LOGGER = Logger.getLogger(ListGuildPlaylistCommand.class.getName());

    private PlaylistDAO playlistDAO;

    public ListGuildPlaylistCommand() {
        this.playlistDAO = PlaylistDAO.getInstance();
        this.name = "gplaylists";
        this.help = "Returns all of the playlists the guild has.";
        this.category = CommandCategories.VOICE;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

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
