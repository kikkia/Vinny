package com.bot.models;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InternalVoiceChannel that = (InternalVoiceChannel) o;
        return isVoiceEnabled == that.isVoiceEnabled &&
                id.equals(that.id) &&
                guildId.equals(that.guildId) &&
                name.equals(that.name);
    }

}
