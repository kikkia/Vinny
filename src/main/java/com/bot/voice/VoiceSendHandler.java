package com.bot.voice;

import com.bot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class VoiceSendHandler extends AudioEventAdapter implements AudioSendHandler {
    private static final Logger LOGGER = Logger.getLogger(VoiceSendHandler.class.getName());

    // Max Duration is in seconds
    public static long MAX_DURATION = 3600;

    private long guildID;
    private long requester;
    private QueuedAudioTrack nowPlaying;
    private Set<String> skipVotes;
    private Queue<QueuedAudioTrack> tracks;
    private List<AudioTrack> trackList;
    private AudioPlayer player;
    private AudioFrame lastFrame;
    private Bot bot;
    private boolean repeat;

    public VoiceSendHandler(long guildID, AudioPlayer player, Bot bot) {
        this.player = player;
        this.guildID = guildID;
        this.bot = bot;
        skipVotes = new HashSet<>();
        tracks = new LinkedBlockingQueue<>();
        trackList = new LinkedList<>();
        nowPlaying = null;
        repeat = false;
    }

    public void queueTrack(AudioTrack track, long user) {
        if (player.getPlayingTrack() == null) {
            player.playTrack(track);
            requester = user;
            nowPlaying = new QueuedAudioTrack(track, user);
        }
        else {
            tracks.add(new QueuedAudioTrack(track, user));
        }
    }

    public void stop() {
        tracks.clear();
        player.stopTrack();
        player.destroy();
        nowPlaying = null;
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
        return lastFrame.getData();
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason == AudioTrackEndReason.FINISHED && repeat) {
            queueTrack(track.makeClone(), requester);
        }
        QueuedAudioTrack nextTrack = tracks.poll();
        trackList.remove(0);
        requester = nextTrack.getRequesterID();
        player.playTrack(nextTrack.getTrack());
        nowPlaying = nextTrack;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public static boolean isSongTooLong(AudioTrack track) {
        return track.getDuration() >= MAX_DURATION * 1000;
    }

    public QueuedAudioTrack getNowPlaying() {
        return nowPlaying;
    }

    public boolean isPlaying() {
        return nowPlaying != null;
    }

    public Queue<QueuedAudioTrack> getTracks() {
        return tracks;
    }
}
