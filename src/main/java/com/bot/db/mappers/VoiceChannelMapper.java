package com.bot.db.mappers;

import com.bot.models.InternalVoiceChannel;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VoiceChannelMapper {

    public static InternalVoiceChannel mapSetToInternalVoiceChannel(ResultSet set) throws SQLException {
        return new InternalVoiceChannel(
                set.getString("id"),
                set.getString("guild"),
                set.getString("name"),
                set.getBoolean("voice_enabled")
        );
    }
}
