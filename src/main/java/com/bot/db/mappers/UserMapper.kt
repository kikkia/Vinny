package com.bot.db.mappers

import com.bot.models.InternalUser
import java.sql.ResultSet
import java.sql.SQLException

object UserMapper {
    @JvmStatic
    @Throws(SQLException::class)
    fun mapSetToUser(set: ResultSet): InternalUser {
        return InternalUser(set.getString("id"),
                set.getBoolean("donor"),
                set.getBoolean("reviewer"))
    }
}