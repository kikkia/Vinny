package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.utils.FormattingUtils;
import com.bot.voice.QueuedAudioTrack;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import dev.arbjerg.lavalink.client.protocol.Track;
import net.dv8tion.jda.api.EmbedBuilder;

public class NowPlayingCommand extends VoiceCommand {

    public NowPlayingCommand() {
        this.name = "nowplaying";
        this.aliases = new String[]{"np", "playing"};
        this.help = "Displays information about the currently playing song.";
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "NowPlaying")
    protected void executeCommand(CommandEvent commandEvent) {
        VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
        if (handler == null) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " I am not connected to a voice channel.");
        } else {
            if (handler.getNowPlaying() == null) {
                commandEvent.replyWarning("I am not currently playing any tracks.");
            } else {
                QueuedAudioTrack nowPlaying = handler.getNowPlaying();
                String currentTrackTime = FormattingUtils.msToMinSec(nowPlaying.getTrack().getInfo().getPosition());
                String totalDuration = FormattingUtils.msToMinSec(nowPlaying.getTrack().getInfo().getLength());
                Track track = nowPlaying.getTrack();

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setAuthor(track.getInfo().getAuthor());
                embedBuilder.setDescription("[" + track.getInfo().getTitle() + "](" + track.getInfo().getUri() + ")");
                embedBuilder.addField("Time", currentTrackTime + " / " + totalDuration, false);
                embedBuilder.addField("Stream", String.valueOf(track.getInfo().isStream()), false);
                embedBuilder.addField("Volume", String.valueOf(handler.getPlayer().getVolume()), false);

                // If youtube, get the thumbnail
                if (track.getInfo().getUri().contains("www.youtube.com")) {
                    String videoID = track.getInfo().getUri().split("=")[1];
                    embedBuilder.setThumbnail("https://img.youtube.com/vi/" + videoID + "/0.jpg");
                }
                embedBuilder.setColor(FormattingUtils.getColorForTrack(track.getInfo().getUri()));

                commandEvent.reply(embedBuilder.build());
            }
        }
    }
}
