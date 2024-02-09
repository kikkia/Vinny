package com.bot.interactions.commands.owner

import com.bot.ShardingManager
import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.OwnerCommand
import com.jagrosh.jdautilities.command.CommandEvent

class RegisterSlashCommands: OwnerCommand() {
    init {
        this.name = "slash"
        this.help = "Update Vinny's slash commands"
        this.aliases = arrayOf("slashcommands")
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        ShardingManager.getInstance().registerCommandsToAllGuilds()
        commandEvent!!.reactSuccess()
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}