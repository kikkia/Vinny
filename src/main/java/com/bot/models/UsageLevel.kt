package com.bot.models

enum class UsageLevel(val id: Int, val maxSub: Int, val maxScheduled: Int, val premiumServers: Int) {
    BASIC(0, 5, 3, 0),
    DONOR(1, 20, 10, 3),
    UNLIMITED(2, 99999, 9999, 999);

    companion object {
        private val map = values().associateBy(UsageLevel::id)
        fun fromInt(type: Int) = map[type]
    }
}