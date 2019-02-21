package com.bot.models;

import com.bot.utils.CommandCategories;
import com.jagrosh.jdautilities.command.Command;

import java.util.HashMap;
import java.util.Map;

public class InternalGuild {

    private String id;
    private String name;
    private int volume;
    private Map<Command.Category, String> roleRequirements;

    public InternalGuild(String id, String name, int minVolume, String minBaseRole, String minModRole, String minNsfwRole, String minVoiceRole) {
        this.id = id;
        this.name = name;
        this.volume = minVolume;
        this.roleRequirements = new HashMap<>();
        this.roleRequirements.put(CommandCategories.GENERAL, minBaseRole);
        this.roleRequirements.put(CommandCategories.MOD, minModRole);
        this.roleRequirements.put(CommandCategories.NSFW, minNsfwRole);
        this.roleRequirements.put(CommandCategories.VOICE, minVoiceRole);
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

    public String getRequiredPermission(Command.Category category) {
        return roleRequirements.get(category);
    }
}
