package com.bot.commands.meme

import com.bot.commands.MemeCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import org.springframework.stereotype.Component

@Component
open class KappaCommand : MemeCommand() {

    private val kappa = "░░░░░░░░░░░░░░░░░░\n" +
            "░░░░▄▀▀▀▀▀█▀▄▄▄▄░░░░\n" +
            "░░▄▀▒▓▒▓▓▒▓▒▒▓▒▓▀▄░░\n" +
            "▄▀▒▒▓▒▓▒▒▓▒▓▒▓▓▒▒▓█░\n" +
            "█▓▒▓▒▓▒▓▓▓░░░░░░▓▓█░\n" +
            "█▓▓▓▓▓▒▓▒░░░░░░░░▓█░\n" +
            "▓▓▓▓▓▒░░░░░░░░░░░░█░\n" +
            "▓▓▓▓░░░░▄▄▄▄░░░▄█▄▀░\n" +
            "░▀▄▓░░▒▀▓▓▒▒░░█▓▒▒░░\n" +
            "▀▄░░░░░░░░░░░░▀▄▒▒█░\n" +
            "░▀░▀░░░░░▒▒▀▄▄▒▀▒▒█░\n" +
            "░░▀░░░░░░▒▄▄▒▄▄▄▒▒█░\n" +
            "░░░▀▄▄▒▒░░░░▀▀▒▒▄▀░░\n" +
            "░░░░░▀█▄▒▒░░░░▒▄▀░░░\n" +
            "░░░░░░░░▀▀█▄▄▄▄▀"

    init {
        this.name = "kappa"
        this.help = "prints a kappa face"
    }

    @Trace(operationName = "executeCommand", resourceName = "Kappa")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply(kappa)
    }
}
