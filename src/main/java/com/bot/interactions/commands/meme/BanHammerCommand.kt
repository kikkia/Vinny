package com.bot.interactions.commands.meme

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.MemeCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class BanHammerCommand : MemeCommand() {

    private val hammer = "░░░░░░░░░░░░\n" +
            " ▄████▄░░░░░░░░░░░░░░░░░░░░\n" +
            "██████▄░░░░░░▄▄▄░░░░░░░░░░\n" +
            "░███▀▀▀▄▄▄▀▀▀░░░░░░░░░░░░░\n" +
            "░░░▄▀▀▀▄░░░█▀▀▄░▄▀▀▄░█▄░█░\n" +
            "░░░▄▄████░░█▀▀▄░█▄▄█░█▀▄█░\n" +
            "░░░░██████░█▄▄▀░█░░█░█░▀█░\n" +
            "░░░░░▀▀▀▀░░░░░░░░░░░░░░░░░"

    init {
        this.name = "hammer"
        this.help = "bring out the hammer"
    }

    @Trace(operationName = "executeCommand", resourceName = "BanHammer")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply(hammer)
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}
