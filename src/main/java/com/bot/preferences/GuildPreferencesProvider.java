package com.bot.preferences;

import com.bot.models.Alias;
import com.jagrosh.jdautilities.command.GuildSettingsProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GuildPreferencesProvider implements GuildSettingsProvider {

    private List<String> prefixes;
    private Map<String, Alias> aliases;
    private String guildId;
    private int dVolume;

    public GuildPreferencesProvider(List<String> prefixes, Map<String, Alias> aliases, String guildId, int dVolume) {
        this.prefixes = prefixes;
        this.aliases = aliases;
        this.guildId = guildId;
        this.dVolume = dVolume;
    }

    @Override
    public Collection<String> getPrefixes() {
        return this.prefixes;
    }

    public Map<String, Alias> getAliases() {
        return aliases;
    }

    public String getGuildId() {
        return guildId;
    }

    public int getdVolume() {
        return dVolume;
    }
}
