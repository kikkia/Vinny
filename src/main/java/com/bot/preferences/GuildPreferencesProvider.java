package com.bot.preferences;

import com.jagrosh.jdautilities.command.GuildSettingsProvider;

import java.util.Collection;
import java.util.List;

public class GuildPreferencesProvider implements GuildSettingsProvider {

    List<String> prefixes;
    String guildId;

    public GuildPreferencesProvider(List<String> prefixes, String guildId) {
        this.prefixes = prefixes;
        this.guildId = guildId;
    }

    @Override
    public Collection<String> getPrefixes() {
        return this.prefixes;
    }
}
