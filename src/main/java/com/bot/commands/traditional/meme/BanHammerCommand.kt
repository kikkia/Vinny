package com.bot.commands.traditional.meme

import com.bot.commands.traditional.MemeCommand
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
}
