package com.bot.utils;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;

public class GuildUtils {

    public static Role getHighestRole(Guild guild) {
        Role highest = null;
        for ( Role r : guild.getRoles()) {
            if (highest == null)
                highest = r;
            else if (highest.getPosition() < r.getPosition())
                highest = r;
        }
        return highest;
    }
}
