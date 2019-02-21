package com.bot.db.mappers;

import com.bot.models.InternalGuildMembership;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GuildMembershipMapper {
    public static InternalGuildMembership mapGuildMembership(ResultSet set) throws SQLException {
        return new InternalGuildMembership(set.getString("gm.user_id"),
                set.getString("gm.guild"),
                set.getBoolean("gm.can_use_bot"));
    }
}
