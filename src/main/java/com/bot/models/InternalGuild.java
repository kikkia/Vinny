package com.bot.models;

import com.bot.preferences.GuildPreferencesProvider;
import com.bot.utils.CommandCategories;
import com.jagrosh.jdautilities.command.Command;

import java.util.*;

public class InternalGuild {

    private String id;
    private String name;
    private int volume;
    private Map<Command.Category, String> roleRequirements;
    private String prefixes;
    private Map<String, Alias> aliases;

    public InternalGuild(String id, String name, int minVolume, String minBaseRole, String minModRole, String minNsfwRole, String minVoiceRole, String prefixes) {
        this.id = id;
        this.name = name;
        this.volume = minVolume;
        this.roleRequirements = new HashMap<>();
        this.roleRequirements.put(CommandCategories.GENERAL, minBaseRole);
        this.roleRequirements.put(CommandCategories.MODERATION, minModRole);
        this.roleRequirements.put(CommandCategories.NSFW, minNsfwRole);
        this.roleRequirements.put(CommandCategories.VOICE, minVoiceRole);
        this.prefixes = prefixes;
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
        // Overriding the permissions for reddit and meme commands with the ones for GENERAL
        if (category.getName().equals("reddit") || category.getName().equals("meme")) {
            return roleRequirements.get(CommandCategories.GENERAL);
        }

        return roleRequirements.get(category);
    }

    public String getPrefixes() {
        return prefixes;
    }

    public ArrayList<String> getPrefixList() {
        if (prefixes == null)
            return new ArrayList<>();

        return new ArrayList<>(Arrays.asList(prefixes.split(" ")));
    }

    public GuildPreferencesProvider getGuildPreferencesProvider() {
        // Return null if no prefixes are set
        if(prefixes == null || prefixes.isEmpty())
            return null;

        // We use a space as a delimiter in the db as it is impossible for it to be uses in a prefix (as jda splits args using it)
        return new GuildPreferencesProvider(Arrays.asList(prefixes.split(" ")), aliases, id);
    }

    public Map<String, Alias> getAliasList() {
        return aliases;
    }

    public void setAliasList(Map<String, Alias> aliases) {
        this.aliases = aliases;
    }
}
