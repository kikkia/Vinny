package com.bot.preferences;

import com.bot.caching.GuildCache;
import com.bot.db.GuildDAO;
import com.bot.models.InternalGuild;
import com.jagrosh.jdautilities.command.GuildSettingsManager;
import net.dv8tion.jda.core.entities.Guild;

import javax.annotation.Nullable;
import java.util.logging.Logger;

public class GuildPreferencesManager implements GuildSettingsManager {
    private static final Logger LOGGER = Logger.getLogger(GuildPreferencesManager.class.getName());

    private GuildDAO guildDAO;
    private GuildCache cache;

    public GuildPreferencesManager() {
        guildDAO = GuildDAO.getInstance();
        cache = GuildCache.getInstance();
    }

    @Nullable
    @Override
    public GuildPreferencesProvider getSettings(Guild g) {

        InternalGuild guild = guildDAO.getGuildById(g.getId(), true);
        if (guild == null) {
            // If guild is not in the db return nothing
            LOGGER.warning("Guild not found in db when getting settings: " + g.getId());
            return null;
        }
        return guild.getGuildPreferencesProvider();
    }
}
