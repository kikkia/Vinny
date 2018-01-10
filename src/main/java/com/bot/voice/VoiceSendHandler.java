package com.bot.voice;

import com.bot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.User;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class VoiceSendHandler extends AudioEventAdapter implements AudioSendHandler {

    // Max Duration is in seconds
    public static long MAX_DURATION = 3600;

    private long guildID;
    private Set<String> skipVotes;
    private Queue<QueuedAudioTrack> tracks;
    private List<AudioTrack> trackList;
    private AudioPlayer player;
    private AudioFrame lastFrame;
    private Bot bot;
    // TODO: Maybe go with the track scheduler approach for cleanliness

    public VoiceSendHandler(long guildID, AudioPlayer player, Bot bot) {
        this.player = player;
        this.guildID = guildID;
        this.bot = bot;
        skipVotes = new HashSet<>();
        tracks = new LinkedBlockingQueue<>();
        trackList = new LinkedList<>();
    }

    public void queueTrack(AudioTrack track, User user) {
        if (player.getPlayingTrack() == null) {
            player.playTrack(track);
        }
        else {
            tracks.add(new QueuedAudioTrack(track, user.getIdLong()));
        }
    }

    public boolean pause() {
        if (player.isPaused()) {
            player.setPaused(false);
            return true;
        }
        else if (player.getPlayingTrack() == null) {
            return false;
        }
        else {
            player.setPaused(true);
            return true;
        }
    }

    public AudioPlayer getPlayer()  {
        return player;
    }

    @Override
    public boolean canProvide() {
        lastFrame = player.provide();
        return lastFrame != null;
    }

    @Override
    public byte[] provide20MsAudio() {
        return lastFrame.data;
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    public static boolean isSongTooLong(AudioTrack track) {
        return track.getDuration() >= MAX_DURATION * 1000;
    }
}
