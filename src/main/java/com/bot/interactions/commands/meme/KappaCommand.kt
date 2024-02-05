package com.bot.interactions.commands.meme

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.MemeCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class KappaCommand : MemeCommand() {

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

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}
