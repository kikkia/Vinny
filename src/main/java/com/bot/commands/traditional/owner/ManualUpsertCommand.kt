package com.bot.commands.traditional.owner

import com.bot.ShardingManager
import com.bot.commands.traditional.OwnerCommand
import com.jagrosh.jdautilities.command.CommandEvent

class ManualUpsertCommand : OwnerCommand() {

    init {
        this.name = "upsert"
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        val guild = commandEvent!!.args
        val shardingManager = ShardingManager.getInstance()
        for (shard in shardingManager.shards) {
            if (guild.isNotEmpty()) {
                shardingManager.commandClientImpl.upsertInteractions(shard.value.jda, guild)
            } else {
                shardingManager.commandClientImpl.upsertInteractions(shard.value.jda)
            }
        }
        commandEvent.reactSuccess()
    }
}