package com.bot.commands.traditional.owner

import com.bot.commands.traditional.OwnerCommand
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
}