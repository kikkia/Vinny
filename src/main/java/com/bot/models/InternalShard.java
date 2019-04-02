package com.bot.models;

import com.bot.voice.VoiceSendHandler;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.managers.AudioManager;

public class InternalShard {
    private int id;
    private JDA jda;
    private int activeVoiceConnectionsCount = 0;
    private int idleVoiceConnectionCount = 0;
    private int queuedTracksCount = 0;
    private int usersInVoiceCount = 0;

    public InternalShard(int id, JDA jda) {
        this.id = id;
        this.jda = jda;
    }

    public int getId() {
        return id;
    }

    public int getActiveVoiceConnectionsCount() {
        return activeVoiceConnectionsCount;
    }

    public int getIdleVoiceConnectionCount() {
        return idleVoiceConnectionCount;
    }

    public int getUsersInVoiceCount() {
        return usersInVoiceCount;
    }

    public int getQueuedTracksCount() {
        return queuedTracksCount;
    }

    public synchronized void updateStatistics() {
        activeVoiceConnectionsCount = 0;
        idleVoiceConnectionCount = 0;
        usersInVoiceCount = 0;
        queuedTracksCount = 0;

        for (AudioManager manager : jda.getAudioManagers()) {
            VoiceSendHandler handler = (VoiceSendHandler) manager.getSendingHandler();
            // Update active connections
            if (manager.isConnected() && handler.isPlaying()) {
                activeVoiceConnectionsCount++;
                usersInVoiceCount += manager.getConnectedChannel().getMembers().size() - 1;
            }

            // Update idle connection count
            if (manager.isConnected() && !handler.isPlaying()) {
                idleVoiceConnectionCount++;
                usersInVoiceCount += manager.getConnectedChannel().getMembers().size() - 1;
            }

            if (handler != null) {
                if (handler.getNowPlaying() != null)
                    queuedTracksCount++;

                queuedTracksCount += handler.getTracks().size();
            }
        }
    }

    public int getServerCount() {return jda.getGuilds().size();}

    public int getUserCount() {return jda.getUsers().size();}

    public JDA getJda() {
        return jda;
    }
}
