package com.bot.commands.traditional.general

import com.bot.ShardingManager
import com.bot.commands.traditional.GeneralCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import net.dv8tion.jda.api.EmbedBuilder

/**
 * This is a "Dark" command. It should not be locked down to just the owner, but it is pretty
 * useless to normal users.
 */
class ShardStatsCommand : GeneralCommand() {
    init {
        this.name = "shardstats"
        this.guildOnly = false
        this.hidden = true
    }

    @Trace(operationName = "executeCommand", resourceName = "ShardStats")
    override fun executeCommand(commandEvent: CommandEvent) {

        // Scan all shards and make some relevant info.
        for ((key, value) in ShardingManager.getInstance().shards) {
            val jda = value.jda
            val builder = EmbedBuilder()
            builder.setTitle("Shard: $key")
            builder.addField("Guilds", jda.guilds.size.toString(), true)
            builder.addField("Users", jda.users.size.toString(), true)
            builder.addField("Ping", jda.gatewayPing.toString() + "ms", true)
            builder.addField("Response Total", jda.responseTotal.toString(), true)
            commandEvent.reply(builder.build())
        }
        commandEvent.reply("You are on shard: " + commandEvent.jda.shardInfo.shardId)
    }
}
