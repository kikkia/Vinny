package com.bot.voice;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import static com.bot.utils.FormattingUtils.msToMinSec;

public class QueuedAudioTrack {
    private String requesterName;
    private long requesterID;
    private AudioTrack track;

    public QueuedAudioTrack(AudioTrack track, String requesterName, long requesterID) {
        this.track = track;
        this.requesterName = requesterName;
        this.requesterID = requesterID;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public long getRequesterID() {return requesterID;}

    public AudioTrack getTrack() {
        return track;
    }

    @Override
    public String toString() {
        return "[" + msToMinSec(track.getDuration()) + "] *" + track.getInfo().title + "* requested by <@" + requesterID + ">";
    }
}
