package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.db.PlaylistDAO;
import com.bot.models.AudioTrack;
import com.bot.models.Playlist;
import com.bot.voice.LoadHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.Permission;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoadMyPlaylistCommand extends Command {
    private static final Logger LOGGER = Logger.getLogger(LoadMyPlaylistCommand.class.getName());
    private PlaylistDAO playlistDAO;
    private Bot bot;

    public LoadMyPlaylistCommand(Bot bot) {
        this.name = "loadmyplaylist";
        this.arguments = "<playlist id|playlist name>";
        this.help = "Loads one of your playlists. You must either specify the id or the name of the playlist.";
        this.playlistDAO = PlaylistDAO.getInstance();
        this.bot = bot;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
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
        String userId = commandEvent.getAuthor().getId();
        playlist = playlistName != null ? playlistDAO.getPlaylistForUserByName(userId, playlistName) :
                playlistDAO.getPlaylistForUserById(userId, playlistId);

        // If no playlist found then return
        // TODO: Custom exception classes for this stuff.
        if (playlist == null) {
            LOGGER.log(Level.WARNING, "No playlist found for id: " + playlistId + " or name: " + playlistName);
            commandEvent.reply(commandEvent.getClient().getWarning() + " Playlist not found! Please check the id/name.");
            return;
        }

        // Check voice perms and what not
        if (commandEvent.getMember().getVoiceState().getChannel() == null) {
           commandEvent.reply(commandEvent.getClient().getWarning() + " You are not in a voice channel! Please join one to use this command.");
            return;
        }
        else if (!commandEvent.getSelfMember().hasPermission(commandEvent.getMember().getVoiceState().getChannel(), Permission.VOICE_CONNECT)) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " I don't have permission to join your voice channel. :cry:");
            return;
        }
        else if (!commandEvent.getSelfMember().hasPermission(commandEvent.getMember().getVoiceState().getChannel(), Permission.VOICE_SPEAK)){
            commandEvent.reply(commandEvent.getClient().getWarning() + " I don't have permission to speak in your voice channel. :cry:");
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
