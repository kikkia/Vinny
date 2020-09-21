package com.bot.commands.meme

import com.bot.commands.MemeCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class SaltCommand : MemeCommand() {

    private val salt = "▒▒▒▒▒▒▄▄██████▄\n" +
            "▒▒▒▒▒▒▒▒▒▒▄▄████████████▄\n" +
            "▒▒▒▒▒▒▄▄██████████████████\n" +
            "▒▒▒▄████▀▀▀██▀██▌███▀▀▀████\n" +
            "▒▒▐▀████▌▀██▌▀▐█▌████▌█████▌\n" +
            "▒▒█▒▒▀██▀▀▐█▐█▌█▌▀▀██▌██████\n" +
            "▒▒█▒▒▒▒████████████████████▌\n" +
            "▒▒▒▌▒▒▒▒█████░░░░░░░██████▀\n" +
            "▒▒▒▀▄▓▓▓▒███░░░░░░█████▀▀\n" +
            "▒▒▒▒▀░▓▓▒▐█████████▀▀▒\n" +
            "▒▒▒▒▒░░▒▒▐█████▀▀▒▒▒▒▒▒\n" +
            "▒▒░░░░░▀▀▀▀▀▀▒▒▒▒▒▒▒▒▒\n" +
            "▒▒▒░░░░░░░░▒▒"

    init {
        this.name = "salt"
    }

    //@trace(operationName = "executeCommand", resourceName = "Salt")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply(salt)
    }
}
