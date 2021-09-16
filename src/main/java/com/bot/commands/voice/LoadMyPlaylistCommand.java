package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.commands.VoiceCommand;
import com.bot.db.PlaylistDAO;
import com.bot.models.AudioTrack;
import com.bot.models.Playlist;
import com.bot.voice.LoadHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

@Component
public class LoadMyPlaylistCommand extends VoiceCommand {
    private PlaylistDAO playlistDAO;
    private Bot bot;

    public LoadMyPlaylistCommand(Bot bot, PlaylistDAO playlistDAO) {
        this.name = "loadmyplaylist";
        this.arguments = "<playlist id|playlist name>";
        this.help = "Loads one of your playlists. You must either specify the id or the name of the playlist.";

        this.playlistDAO = playlistDAO;
        this.bot = bot;
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "LoadMyPlaylists")
    protected void executeCommand(CommandEvent commandEvent) {
        int playlistId = -1;
        String playlistName = null;
        Playlist playlist;
        try {
            // Check if we are given a number (implies playlist id)
            playlistId = Integer.parseInt(commandEvent.getArgs());
        } catch (NumberFormatException e) {
            // if number parsing fails we look for the name;
            playlistName = commandEvent.getArgs();
        }

        if (playlistName != null && playlistName.isEmpty()) {
            commandEvent.replyWarning("You must specify a playlist name or id to load it.");
            return;
        }
        String userId = commandEvent.getAuthor().getId();
        playlist = playlistName != null ? playlistDAO.getPlaylistForUserByName(userId, playlistName) :
                playlistDAO.getPlaylistForUserById(userId, playlistId);

        // If no playlist found then return
        // TODO: Custom exception classes for this stuff.
        if (playlist == null) {
            logger.log(Level.WARNING, "No playlist found for id: " + playlistId + " or name: " + playlistName);
            commandEvent.reply(commandEvent.getClient().getWarning() + " Playlist not found! Please check the id/name.");
            return;
        }

        // If not in voice, join
        if (!commandEvent.getGuild().getAudioManager().isConnected()) {
            commandEvent.getGuild().getAudioManager().openAudioConnection(commandEvent.getMember().getVoiceState().getChannel());
        }

        // Queue up the tracks
        for (AudioTrack track : playlist.getTracks()) {
            bot.getManager().loadItemOrdered(commandEvent.getGuild(), track.getUrl(), new LoadHandler(bot, commandEvent));
        }

        commandEvent.reply(commandEvent.getClient().getSuccess() + " Loaded playlist!");

    }
}
