package com.bot.commands.meme

import com.bot.commands.MemeCommand
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

    //@trace(operationName = "executeCommand", resourceName = "BanHammer")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply(hammer)
    }
}
