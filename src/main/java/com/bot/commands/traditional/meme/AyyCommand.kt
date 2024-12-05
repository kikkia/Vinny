package com.bot.commands.traditional.meme

import com.bot.commands.traditional.MemeCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class AyyCommand : MemeCommand() {

    private val lmao = "░░░░█▒▒▄▀▀▀▀▀▄▄▒▒▒▒▒▒▒▒▒▄▄▀▀▀▀▀▀▄\n" +
            "    ░░▄▀▒▒▒▄█████▄▒█▒▒▒▒▒▒▒█▒▄█████▄▒█\n" +
            "    ░█▒▒▒▒▐██▄████▌▒█▒▒▒▒▒█▒▐██▄████▌▒█\n" +
            "    ▀▒▒▒▒▒▒▀█████▀▒▒█▒░▄▒▄█▒▒▀█████▀▒▒▒█\n" +
            "    ▒▒▐▒▒▒░░░░▒▒▒▒▒█▒░▒▒▀▒▒█▒▒▒▒▒▒▒▒▒▒▒▒█\n" +
            "    ▒▌▒▒▒░░░▒▒▒▒▒▄▀▒░▒▄█▄█▄▒▀▄▒▒▒▒▒▒▒▒▒▒▒▌\n" +
            "    ▒▌▒▒▒▒░▒▒▒▒▒▒▀▄▒▒█▌▌▌▌▌█▄▀▒▒▒▒▒▒▒▒▒▒▒▐\n" +
            "    ▒▐▒▒▒▒▒▒▒▒▒▒▒▒▒▌▒▒▀███▀▒▌▒▒▒▒▒▒▒▒▒▒▒▒▌\n" +
            "    ▀▀▄▒▒▒▒▒▒▒▒▒▒▒▌▒▒▒▒▒▒▒▒▒▐▒▒▒▒▒▒▒▒▒▒▒█\n" +
            "    ▀▄▒▀▄▒▒▒▒▒▒▒▒▐▒▒▒▒▒▒▒▒▒▄▄▄▄▒▒▒▒▒▒▄▄▀\n" +
            "    ▒▒▀▄▒▀▄▀▀▀▄▀▀▀▀▄▄▄▄▄▄▄▀░░░░▀▀▀▀▀▀\n" +
            "    ▒▒▒▒▀▄▐▒▒▒▒▒▒▒▒▒▒▒▒▒▐\n" +
            "    ░▄▄▄░░▄░░▄░▄░░▄░░▄░░░░▄▄░▄▄░░░▄▄▄░░░▄▄▄\n" +
            "    █▄▄▄█░█▄▄█░█▄▄█░░█░░░█░░█░░█░█▄▄▄█░█░░░█\n" +
            "    █░░░█░░█░░░░█░░░░█░░░█░░█░░█░█░░░█░█░░░█\n" +
            "    ▀░░░▀░░▀░░░░▀░░░░▀▀▀░░░░░░░░░▀░░░▀░▀▄▄▄▀\uFEFF"

    init {
        this.name = "ayy"
        this.help = "lmao"
    }

    @Trace(operationName = "executeCommand", resourceName = "Ayy")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply(lmao)
    }
}
