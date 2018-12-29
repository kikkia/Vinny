package com.bot.models;

public class UserMembership {
    private String id;
    private String name;
    private String guildId;
    private boolean canUseBot;

    public UserMembership(String id, String name, String guildId, boolean canUseBot) {
        this.id = id;
        this.name = name;
        this.guildId = guildId;
        this.canUseBot = canUseBot;
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

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public boolean isCanUseBot() {
        return canUseBot;
    }

    public void setCanUseBot(boolean canUseBot) {
        this.canUseBot = canUseBot;
    }
}
