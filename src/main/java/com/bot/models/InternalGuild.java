package com.bot.models;

public class InternalGuild {

    private String id;
    private String name;
    private int volume;
    private String min_base_role;
    private String min_mod_role;
    private String min_nsfw_role;
    private String min_voice_role;

    public InternalGuild(String id, String name, int minVolume, String min_base_role, String min_mod_role, String min_nsfw_role, String min_voice_role) {
        this.id = id;
        this.name = name;
        this.volume = minVolume;
        this.min_base_role = min_base_role;
        this.min_mod_role = min_mod_role;
        this.min_nsfw_role = min_nsfw_role;
        this.min_voice_role = min_voice_role;
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

    public String getMin_base_role() {
        return min_base_role;
    }

    public void setMin_base_role(String min_base_role) {
        this.min_base_role = min_base_role;
    }

    public String getMin_mod_role() {
        return min_mod_role;
    }

    public void setMin_mod_role(String min_mod_role) {
        this.min_mod_role = min_mod_role;
    }

    public String getMin_nsfw_role() {
        return min_nsfw_role;
    }

    public void setMin_nsfw_role(String min_nsfw_role) {
        this.min_nsfw_role = min_nsfw_role;
    }

    public String getMin_voice_role() {
        return min_voice_role;
    }

    public void setMin_voice_role(String min_voice_role) {
        this.min_voice_role = min_voice_role;
    }
}
