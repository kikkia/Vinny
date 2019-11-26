package com.bot.models

data class ScheduledCommand(val id: Int, val command: String, val guild: String, val channel: String, val author: String, val interval: Long, val lastRun: Long)