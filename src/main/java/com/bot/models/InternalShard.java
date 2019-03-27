package com.bot.models;

import com.bot.voice.VoiceSendHandler;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.managers.AudioManager;

public class InternalShard {
    private int id;
    private JDA jda;

    public InternalShard(int id, JDA jda) {
        this.id = id;
        this.jda = jda;
    }

    public int getId() {
        return id;
    }

    public int getActiveVoiceConnectionsCount() {
        int count = 0;

        for (AudioManager manager : jda.getAudioManagers()) {
            VoiceSendHandler handler = (VoiceSendHandler) manager.getSendingHandler();
            if (manager.isConnected() && handler.isPlaying()) {
                count++;
            }
        }

        return count;
    }

    public int getIdleVoiceConnectionsCount() {
        int count = 0;

        for (AudioManager manager : jda.getAudioManagers()) {
            VoiceSendHandler handler = (VoiceSendHandler) manager.getSendingHandler();
            if (manager.isConnected() && !handler.isPlaying()) {
                count++;
            }
        }

        return count;
    }

    public int getServerCount() {return jda.getGuilds().size();}

    public int getUserCount() {return jda.getUsers().size();}

    public JDA getJda() {
        return jda;
    }
}
