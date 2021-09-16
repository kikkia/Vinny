package com.bot.commands.meme

import com.bot.commands.MemeCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import org.springframework.stereotype.Component

@Component
open class BanHammerCommand : MemeCommand() {

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
