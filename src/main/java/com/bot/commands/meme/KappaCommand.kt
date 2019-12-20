package com.bot.commands.meme

import com.bot.commands.MemeCommand
import com.jagrosh.jdautilities.command.CommandEvent

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


    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply(kappa)
    }
}
