package com.bot.voice;

import com.bot.exceptions.MaxQueueSizeException;
import com.bot.utils.FormattingUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class VoiceSendHandler extends AudioEventAdapter implements AudioSendHandler {
    // Max Duration is in seconds
    public static long MAX_DURATION = 36009;
    public static int MAX_QUEUE_SIZE = 1000;

    private long requester;
    private String requesterName;
    private TextChannel lastUsedChannel;
    private QueuedAudioTrack nowPlaying;
    private Queue<QueuedAudioTrack> tracks;
    private AudioPlayer player;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;
    private boolean repeatOne;
    private boolean repeatAll;
    private boolean lockVolume;
    private double speed;

    public VoiceSendHandler(AudioPlayer player) {
        this.player = player;
        this.tracks = new LinkedBlockingQueue<>();
        this.nowPlaying = null;
        this.repeatOne = false;
        this.repeatAll = false;
        this.lockVolume = false;

        this.buffer = ByteBuffer.allocate(1024);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
        this.speed = 1.0;
    }

    public void queueTrack(AudioTrack track, long user, String requesterName, TextChannel channel) throws MaxQueueSizeException {
        if (player.getPlayingTrack() == null) {
            player.playTrack(track);
            requester = user;
            lastUsedChannel = channel;
            this.requesterName = requesterName;
            nowPlaying = new QueuedAudioTrack(track, requesterName, user);

            sendNowPlayingUpdate();
        }
        else if (tracks.size() >= MAX_QUEUE_SIZE) {
            throw new MaxQueueSizeException("Error: Loading track will exceed the max queue size of " + MAX_QUEUE_SIZE + "\n" +
                    "Sorry but due to the banning crisis among bots right now this limit is to help keep Vinny running. " +
                    "It will hopefully be raised or removed soon.");
        }
        else {
            tracks.add(new QueuedAudioTrack(track, requesterName, user));
        }
        lastUsedChannel = channel;
    }

    public boolean skipTrack() {
        if (player.getPlayingTrack() == null) {
            return false;
        }
        else if (tracks.size() == 0) {
            return false;
        }
        else {
            QueuedAudioTrack nextTrack = tracks.poll();
            if (nextTrack == null) {
                stop();
            }
            else {
                player.stopTrack();
                requester = nextTrack.getRequesterID();
                player.playTrack(nextTrack.getTrack());
                nowPlaying = nextTrack;

                sendNowPlayingUpdate();
            }
            return true;
        }
    }

    public void stop() {
        tracks.clear();
        player.setPaused(false);
        player.stopTrack();
        player.destroy();
        repeatOne = false;
        repeatAll = false;
        nowPlaying = null;
        setSpeed(1.0);
    }

    public AudioPlayer getPlayer()  {
        return player;
    }

    @Override
    public boolean canProvide() {
        return player.provide(frame);
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return (ByteBuffer) buffer.flip();
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {

        // TODO: cleanup dirty logic in this func
        if (endReason == AudioTrackEndReason.FINISHED) {
            if (repeatOne) {
                try {
                    queueTrack(track.makeClone(), requester, requesterName, lastUsedChannel);
                } catch (MaxQueueSizeException e) {
                    // Unused since we are not adding a track
                }
                return;
            }
        }

        QueuedAudioTrack nextTrack = tracks.peek();
        // If nextTrack is null then we are done
        if (nextTrack == null) {
            if (repeatAll) {
                // Just repeat the playing track
                try {
                    queueTrack(track.makeClone(), requester, requesterName, lastUsedChannel);
                } catch (MaxQueueSizeException e) {
                    // Unused since we are not adding a track
                }
            } else {
                stop();
            }
        }
        else if (endReason.mayStartNext){
            requester = nextTrack.getRequesterID();
            requesterName = nextTrack.getRequesterName();
            player.playTrack(nextTrack.getTrack());
            nowPlaying = nextTrack;

            sendNowPlayingUpdate();
            tracks.poll(); // Since we peek above, take the track off of the queue
            if (repeatAll) {
                try {
                    queueTrack(track.makeClone(), requester, requesterName, lastUsedChannel);
                } catch (MaxQueueSizeException e) {
                    // Unused since we are not adding a track
                }
            }
        }
    }

    public boolean isRepeatOne() {
        return repeatOne;
    }

    public void setRepeatOne(boolean repeatOne) {
        // If repeat all is on, and we are turning on repeat one, turn off repeatAll
        if (repeatAll && repeatOne) {
            repeatAll = false;
        }
        this.repeatOne = repeatOne;
    }

    public boolean isRepeatAll() {
        return repeatAll;
    }

    public void setRepeatAll(boolean repeatAll) {
        // If repeat one is on, and we are turning on repeat all, turn off repeatOne
        if (repeatOne && repeatAll) {
            repeatOne = false;
        }
        this.repeatAll = repeatAll;
    }

    public static boolean isSongTooLong(AudioTrack track) {
        return track.getDuration() >= MAX_DURATION * 1000 && !track.getInfo().isStream;
    }

    public QueuedAudioTrack getNowPlaying() {
        return nowPlaying;
    }

    public boolean isPlaying() {
        return nowPlaying != null && !player.isPaused();
    }

    public boolean isActive() {return nowPlaying != null;}

    public Queue<QueuedAudioTrack> getTracks() {
        return tracks;
    }

    public boolean toggleVolumeLock() {
        this.lockVolume = !lockVolume;
        return lockVolume;
    }

    public boolean isLocked() {
        return lockVolume;
    }

    public void setTracks(Queue<QueuedAudioTrack> queue) {
        this.tracks = queue;
    }
  
    private void sendNowPlayingUpdate() {
        // Refresh the last channel to make sure we dont have a stale one
        lastUsedChannel = lastUsedChannel.getJDA().getTextChannelById(lastUsedChannel.getId());

        // If we sent the last message in the channel then just edit it
        lastUsedChannel.getHistory().retrievePast(1).queue(m -> {
            Message lastMessage = m.get(0);
            if (lastMessage.getAuthor().getId().equals(lastUsedChannel.getJDA().getSelfUser().getId())) {
                lastMessage.editMessage(FormattingUtils.getAudioTrackEmbed(nowPlaying, player.getVolume())).queue();
            } else {
                lastUsedChannel.sendMessage(FormattingUtils.getAudioTrackEmbed(nowPlaying, player.getVolume())).queue();
            }
        });
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
//        this.speed = speed;
//
//        this.player.setFilterFactory((track, format, output)->{
//            TimescalePcmAudioFilter audioFilter = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
//            audioFilter.setSpeed(speed);
//            return Collections.singletonList(audioFilter);
//        });
    }

    // Send an update to the last used channel to announce that a reboot is happening.
    public void sendUpdateToLastUsedChannel(String message) {
        lastUsedChannel = lastUsedChannel.getJDA().getTextChannelById(lastUsedChannel.getId());

        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor("Kikkia");
        builder.setColor(Color.RED);
        builder.setTitle("Voice maintenance announcement!");
        builder.setDescription(message);

        if (lastUsedChannel != null)
            lastUsedChannel.sendMessage(builder.build()).queue();
    }

    public void shuffleTracks() {
        List<QueuedAudioTrack> trackList = new ArrayList<QueuedAudioTrack>(Arrays.asList(tracks.toArray(new QueuedAudioTrack[]{})));
        Collections.shuffle(trackList);
        tracks = new LinkedList<>(trackList);
    }

    public void clearQueue() {
        tracks.clear();
    }
}
