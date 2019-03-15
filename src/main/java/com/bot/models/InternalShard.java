package com.bot.models;

import net.dv8tion.jda.core.JDA;

public class InternalShard {
    private int id;
    private int voiceStreamsCount;
    private JDA jda;

    public InternalShard(int id, JDA jda) {
        this.id = id;
        this.voiceStreamsCount = 0;
        this.jda = jda;
    }

    public int getId() {
        return id;
    }

    public int getVoiceStreamsCount() {
        return voiceStreamsCount;
    }

    public int getServerCount() {return jda.getGuilds().size();}

    public void addVoiceStream() {
        voiceStreamsCount++;
    }

    public void removeVoiceStream() {
        voiceStreamsCount--;
    }

    public JDA getJda() {
        return jda;
    }
}
