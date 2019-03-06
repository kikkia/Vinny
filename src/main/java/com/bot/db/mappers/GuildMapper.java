package com.bot.db.mappers;

import com.bot.models.InternalGuild;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GuildMapper {
    public static InternalGuild mapSetToGuild(ResultSet set) throws SQLException {
        return new InternalGuild(set.getString("id"),
                set.getString("name"),
                set.getInt("default_volume"),
                set.getString("min_base_role_id"),
                set.getString("min_mod_role_id"),
                set.getString("min_nsfw_role_id"),
                set.getString("min_voice_role_id"),
                set.getString("prefixes"));
    }

}
