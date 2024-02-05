package com.bot.interactions.commands.meme

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.MemeCommand
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

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}
