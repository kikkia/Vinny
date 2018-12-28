package com.bot.models;

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
}
