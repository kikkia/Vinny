package com.bot.voice;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class QueuedAudioTrack {
    private long requesterID;
    private AudioTrack track;

    public QueuedAudioTrack(AudioTrack track, long requesterID) {
        this.track = track;
        this.requesterID = requesterID;
    }

    public long getRequesterID() {
        return requesterID;
    }

    public AudioTrack getTrack() {
        return track;
    }

    @Override
    public String toString() {
        return "[" + msToMinSec(track.getDuration()) + "] *" + track.getInfo().title + "* requested by <@" + requesterID + ">";
    }

    //Helper method for song that takes length in Milliseconds and outputs it in a more readable HH:MM:SS format
    private String msToMinSec(long length) {
        int totSeconds = (int)length/1000;
        String seconds = "";
        String minutes = "";
        String hours = "";
        if (totSeconds%60 < 10)
            seconds = "0" + totSeconds%60;
        else
            seconds += totSeconds%60;
        if (totSeconds/60 < 10)
            minutes = "0" + totSeconds/60;
        else if (totSeconds/60 > 59)
            minutes += (totSeconds/60)%60;
        else
            minutes += totSeconds/60;
        if (totSeconds/3600 < 10)
            hours = "0" + (totSeconds/60)/60;
        else
            hours += (totSeconds/60)/60;

        if (hours.equals("00"))
            return minutes + ":" + seconds;
        else {
            if (minutes.length() == 1)
                minutes = "0" + minutes;
            return hours + ":" + minutes + ":" + seconds;
        }
    }

}
