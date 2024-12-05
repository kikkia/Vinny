package com.bot.commands.traditional.meme

import com.bot.commands.traditional.MemeCommand
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

    @Trace(operationName = "executeCommand", resourceName = "Salt")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply(salt)
    }
}
