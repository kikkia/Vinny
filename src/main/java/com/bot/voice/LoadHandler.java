package com.bot.voice;

import com.bot.Bot;
import com.bot.utils.HttpUtils;
import com.bot.utils.Logger;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class LoadHandler implements AudioLoadResultHandler {

    private Bot bot;
    private CommandEvent commandEvent;
    private static Logger logger = new Logger(HttpUtils.class.getName());

    public LoadHandler(Bot bot, CommandEvent commandEvent) {
        this.bot = bot;
        this.commandEvent = commandEvent;
    }

    @Override
    public void trackLoaded(AudioTrack audioTrack) {
        // Since the song comes from the db the length was checked at some point, so no need to check
        bot.getHandler(commandEvent.getGuild()).queueTrack(audioTrack,
                commandEvent.getAuthor().getIdLong(),
                commandEvent.getAuthor().getName(),
                commandEvent.getTextChannel());
    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {
        // Should not occur when loading a track from db
    }

    @Override
    public void noMatches() {
        // should not occur when loading from db
    }

    @Override
    public void loadFailed(FriendlyException e) {
        // If load fails just tell the user.
        commandEvent.reply(commandEvent.getClient().getWarning() + " Failed to load a track: " + e.getMessage());
        logger.severe("Failed to load a track", e);
        logger.severe("Additional info", (Exception) e.getCause());
    }
}
