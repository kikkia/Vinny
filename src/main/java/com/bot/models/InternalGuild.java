package com.bot.models;

public class InternalGuild {

    private String id;
    private String name;
    private int volume;
    private String minBaseRole;
    private String minModRole;
    private String minNsfwRole;
    private String minVoiceRole;

    public InternalGuild(String id, String name, int minVolume, String minBaseRole, String minModRole, String minNsfwRole, String minVoiceRole) {
        this.id = id;
        this.name = name;
        this.volume = minVolume;
        this.minBaseRole = minBaseRole;
        this.minModRole = minModRole;
        this.minNsfwRole = minNsfwRole;
        this.minVoiceRole = minVoiceRole;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int minVolume) {
        this.volume = minVolume;
    }

    public String getMinBaseRole() {
        return minBaseRole;
    }

    public void setMinBaseRole(String minBaseRole) {
        this.minBaseRole = minBaseRole;
    }

    public String getMinModRole() {
        return minModRole;
    }

    public void setMinModRole(String minModRole) {
        this.minModRole = minModRole;
    }

    public String getMinNsfwRole() {
        return minNsfwRole;
    }

    public void setMinNsfwRole(String minNsfwRole) {
        this.minNsfwRole = minNsfwRole;
    }

    public String getMinVoiceRole() {
        return minVoiceRole;
    }

    public void setMinVoiceRole(String minVoiceRole) {
        this.minVoiceRole = minVoiceRole;
    }
}
