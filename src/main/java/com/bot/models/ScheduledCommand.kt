package com.bot.models

import com.bot.utils.FormattingUtils

data class ScheduledCommand(val id: Int, val command: String, val guild: String, val channel: String, val author: String, val interval: Long, val lastRun: Long) {
    override fun toString(): String {
        return "Id:`" + id + "`\nCommand:`" + command + "`\nGuild:`" + guild + "`\nChannel:`" + channel + "`\nAuthor:`" +
                author + "`\nInterval`" + FormattingUtils.getDurationBreakdown(interval) + "`\nLast Run: `" +
                FormattingUtils.getDateForMillis(lastRun) + "`"
    }
}