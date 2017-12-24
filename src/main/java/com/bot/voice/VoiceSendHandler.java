package com.bot.voice;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.audio.AudioSendHandler;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public class VoiceSendHandler extends AudioEventAdapter implements AudioSendHandler {

    private long guildID;
    private Set<String> skipVotes;
    private Queue<QueuedAudioTrack> tracks;
    private List<AudioTrack> trackList;
    private AudioPlayer player;

    public VoiceSendHandler(long guildID, AudioPlayer player, ) {

    }


    @Override
    public boolean canProvide() {
        return false;
    }

    @Override
    public byte[] provide20MsAudio() {
        return new byte[0];
    }
}
