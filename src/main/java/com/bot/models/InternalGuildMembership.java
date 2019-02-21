package com.bot.models;

public class InternalGuildMembership {
    private String user_id;
    private String name;
    private String guildId;
    private boolean canUseBot;

    public InternalGuildMembership(String id, String guildId, boolean canUseBot) {
        this.user_id = id;
        this.guildId = guildId;
        this.canUseBot = canUseBot;
    }

    public String getId() {
        return user_id;
    }

    public void setId(String id) {
        this.user_id = id;
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

    public boolean canUseBot() {
        return canUseBot;
    }

    public void setCanUseBot(boolean canUseBot) {
        this.canUseBot = canUseBot;
    }
}
