package com.bot.db.mappers

import com.bot.models.InternalTextChannel
import java.sql.ResultSet
import java.sql.SQLException

object TextChannelMapper {
    @JvmStatic
    @Throws(SQLException::class)
    fun mapSetToInternalTextChannel(set: ResultSet): InternalTextChannel {
        return InternalTextChannel(
                set.getString("id"),
                set.getString("guild"),
                set.getString("name"),
                set.getBoolean("announcement"),
                set.getBoolean("nsfw_enabled"),
                set.getBoolean("commands_enabled"),
                set.getBoolean("voice_enabled")
        )
    }
}