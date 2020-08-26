package com.bot.db.mappers

import com.bot.models.InternalGuildMembership
import java.sql.ResultSet
import java.sql.SQLException

object GuildMembershipMapper {
    @JvmStatic
    @Throws(SQLException::class)
    fun mapGuildMembership(set: ResultSet): InternalGuildMembership {
        return InternalGuildMembership(set.getString("gm.user_id"),
                set.getString("gm.guild"),
                set.getBoolean("gm.can_use_bot"))
    }
}