package com.bot.models

enum class UsageLevel(val id: Int) {
    BASIC(0),
    DONOR(1),
    UNLIMITED(2);

    companion object {
        private val map = values().associateBy(UsageLevel::id)
        fun fromInt(type: Int) = map[type]
    }
}