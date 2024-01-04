package com.bot.voice;

import dev.arbjerg.lavalink.client.protocol.Track;

import static com.bot.utils.FormattingUtils.msToMinSec;

public class QueuedAudioTrack {
    private String requesterName;
    private long requesterID;
    private Track track;

    public QueuedAudioTrack(Track track, String requesterName, long requesterID) {
        this.track = track;
        this.requesterName = requesterName;
        this.requesterID = requesterID;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public long getRequesterID() {return requesterID;}

    public Track getTrack() {
        return track;
    }

    @Override
    public String toString() {
        return "[" + msToMinSec(track.getInfo().getLength()) + "] *" + track.getInfo().getTitle() + "* requested by " + requesterName;
    }
}
