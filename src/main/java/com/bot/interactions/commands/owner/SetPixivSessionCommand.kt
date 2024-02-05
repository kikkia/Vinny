package com.bot.interactions.commands.owner

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.OwnerCommand
import com.bot.utils.PixivClient
import com.jagrosh.jdautilities.command.CommandEvent

class SetPixivSessionCommand : OwnerCommand() {
    init {
        name = "setpixiv"
    }

    override fun executeCommand(commandEvent: CommandEvent) {
        PixivClient.instance!!.setSession(commandEvent.args)
        commandEvent.reactSuccess()
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}