package com.bot.utils;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;

import java.util.List;

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

    public static String convertListToPrefixesString(List<String> prefixes) {
        StringBuilder sb = new StringBuilder();
        for (String prefix : prefixes) {
            sb.append(prefix);
            sb.append(" ");
        }

        return sb.toString();
    }
}
