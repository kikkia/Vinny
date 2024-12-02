package com.bot.preferences;

import com.bot.db.GuildDAO;
import com.bot.models.InternalGuild;
import com.bot.utils.Logger;
import com.jagrosh.jdautilities.command.GuildSettingsManager;
import net.dv8tion.jda.api.entities.Guild;


public class GuildPreferencesManager implements GuildSettingsManager {
    private static final Logger LOGGER = new Logger(GuildPreferencesManager.class.getName());

    private final GuildDAO guildDAO;

    public GuildPreferencesManager() {
        guildDAO = GuildDAO.getInstance();
    }

    @Override
    public GuildPreferencesProvider getSettings(Guild g) {

        InternalGuild guild = guildDAO.getGuildById(g.getId(), true);
        if (guild == null) {
            // If guild is not in the db return nothing
            LOGGER.warning("Guild not found in db when getting settings: " + g.getId());
            guildDAO.addFreshGuild(g);
            return null;
        }
        return guild.getGuildPreferencesProvider();
    }
}
