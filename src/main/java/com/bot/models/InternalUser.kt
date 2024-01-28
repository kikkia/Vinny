package com.bot.models

import java.sql.ResultSet

data class InternalUser(val id: String, val usageLevel: Int) {
    fun usageLevel() : UsageLevel {
        return UsageLevel.fromInt(usageLevel)!!
    }

    companion object {
        fun mapSetToUser(set: ResultSet) : InternalUser {
            return InternalUser(set.getString("id"), set.getInt("usage_level"))
        }
    }
}