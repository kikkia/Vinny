package com.bot.db.mappers;

import com.bot.models.InternalTextChannel;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TextChannelMapper {

    public static InternalTextChannel mapSetToInternalTextChannel(ResultSet set) throws SQLException {
        return new InternalTextChannel(
                set.getString("id"),
                set.getString("guild"),
                set.getString("name"),
                set.getBoolean("announcement"),
                set.getBoolean("nsfw_enabled"),
                set.getBoolean("commands_enabled"),
                set.getBoolean("voice_enabled")
        );
    }
}
