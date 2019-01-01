package com.bot.models;

import java.util.Objects;

public class InternalTextChannel extends InternalChannel {
    private boolean isAnnouncmentChannel;
    private boolean isNSFWEnabled;
    private boolean isCommandsEnabled;
    private boolean isVoiceEnabled;

    public InternalTextChannel(String id,
                               String guildId,
                               String name,
                               boolean isAnnouncmentChannel,
                               boolean isNSFWEnabled,
                               boolean isCommandsEnabled,
                               boolean isVoiceEnabled) {
        this.id = id;
        this.guildId = guildId;
        this.name = name;
        this.isAnnouncmentChannel = isAnnouncmentChannel;
        this.isCommandsEnabled = isCommandsEnabled;
        this.isNSFWEnabled = isNSFWEnabled;
        this.isVoiceEnabled = isVoiceEnabled;
    }

    public boolean isAnnouncmentChannel() {
        return isAnnouncmentChannel;
    }

    public void setAnnouncmentChannel(boolean announcmentChannel) {
        isAnnouncmentChannel = announcmentChannel;
    }

    public boolean isNSFWEnabled() {
        return isNSFWEnabled;
    }

    public void setNSFWEnabled(boolean NSFWEnabled) {
        isNSFWEnabled = NSFWEnabled;
    }

    public boolean isCommandsEnabled() {
        return isCommandsEnabled;
    }

    public void setCommandsEnabled(boolean commandsEnabled) {
        isCommandsEnabled = commandsEnabled;
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
        InternalTextChannel that = (InternalTextChannel) o;
        return isAnnouncmentChannel == that.isAnnouncmentChannel &&
                isNSFWEnabled == that.isNSFWEnabled &&
                isCommandsEnabled == that.isCommandsEnabled &&
                isVoiceEnabled == that.isVoiceEnabled &&
                id.equals(that.getId()) &&
                guildId.equals(that.guildId) &&
                name.equals(that.name);
    }


}
