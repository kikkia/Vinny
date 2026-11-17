package com.bot.models

import com.bot.ShardingManager
import com.bot.utils.FormattingUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

data class ScheduledCommand(val id: Int, val command: String, val guild: String, val channel: String, val author: String, val interval: Long, val lastRun: Long) {
    override fun toString(): String {
        return "Id:`" + id + "`\nCommand:`" + command + "`\nGuild:`" + guild + "`\nChannel:`" + channel + "`\nAuthor:`" +
                author + "`\nInterval`" + FormattingUtils.getDurationBreakdown(interval) + "`\nLast Run: `" +
                FormattingUtils.getDateForMillis(lastRun) + "`"
    }

    fun toEmbed(): MessageEmbed {
        val channelName = ShardingManager.getInstance().getChannel(channel)?.name ?: channel
        val authorName = ShardingManager.getInstance().getUserFromAnyShard(author)?.name ?: author
        return EmbedBuilder()
            .setTitle("Scheduled Command ID: $id")
            .addField("Command", "`$command`", false)
            .addField("Channel", "#$channelName ($channel)", false)
            .addField("Author", authorName, true)
            .addField("Interval", FormattingUtils.getDurationBreakdown(interval), true)
            .addField("Last Run", FormattingUtils.getDateForMillis(lastRun), true)
            .build()
    }
}