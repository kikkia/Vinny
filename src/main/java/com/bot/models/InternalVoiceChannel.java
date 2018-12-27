package com.bot.models;

public class InternalVoiceChannel extends InternalChannel {
    private boolean isVoiceEnabled;

    public InternalVoiceChannel(String id,
                               String guildId,
                               String name,
                               boolean isVoiceEnabled) {
        this.id = id;
        this.guildId = guildId;
        this.name = name;
        this.isVoiceEnabled = isVoiceEnabled;
    }

    public boolean isVoiceEnabled() {
        return isVoiceEnabled;
    }

    public void setVoiceEnabled(boolean voiceEnabled) {
        isVoiceEnabled = voiceEnabled;
    }
}
