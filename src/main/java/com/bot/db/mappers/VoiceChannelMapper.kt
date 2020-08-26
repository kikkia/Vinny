package com.bot.db.mappers

import com.bot.models.InternalVoiceChannel
import java.sql.ResultSet
import java.sql.SQLException

object VoiceChannelMapper {
    @JvmStatic
    @Throws(SQLException::class)
    fun mapSetToInternalVoiceChannel(set: ResultSet): InternalVoiceChannel {
        return InternalVoiceChannel(
                set.getString("id"),
                set.getString("guild"),
                set.getString("name"),
                set.getBoolean("voice_enabled")
        )
    }
}