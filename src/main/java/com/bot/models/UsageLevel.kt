package com.bot.models

enum class UsageLevel(val id: Int, val maxSub: Int, val maxScheduled: Int) {
    BASIC(0, 5, 3),
    DONOR(1, 20, 10),
    UNLIMITED(2, 99999, 9999);

    companion object {
        private val map = values().associateBy(UsageLevel::id)
        fun fromInt(type: Int) = map[type]
    }
}