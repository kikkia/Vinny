package com.bot.db.mappers;

import com.bot.models.InternalGuildMembership;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GuildMembershipMapper {
    public static InternalGuildMembership mapUserMembership(ResultSet set) throws SQLException {
        return new InternalGuildMembership(set.getString("u.id"),
                set.getString("name"),
                set.getString("g.id"),
                set.getBoolean("u.can_use_bot"));
    }
}
