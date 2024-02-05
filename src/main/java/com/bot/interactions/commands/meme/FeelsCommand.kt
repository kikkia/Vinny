package com.bot.interactions.commands.meme

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.MemeCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class FeelsCommand : MemeCommand() {

    private val feels = "───────▄▀▀▀▀▀▀▀▀▀▀▄▄\n" +
            "────▄▀▀░░░░░░░░░░░░░▀▄\n" +
            "──▄▀░░░░░░░░░░░░░░░░░░▀▄\n" +
            "──█░░░░░░░░░░░░░░░░░░░░░▀▄\n" +
            "─▐▌░░░░░░░░▄▄▄▄▄▄▄░░░░░░░▐▌\n" +
            "─█░░░░░░░░░░░▄▄▄▄░░▀▀▀▀▀░░█\n" +
            "▐▌░░░░░░░▀▀▀▀░░░░░▀▀▀▀▀░░░▐▌\n" +
            "█░░░░░░░░░▄▄▀▀▀▀▀░░░░▀▀▀▀▄░█\n" +
            "█░░░░░░░░░░░░░░░░▀░░░▐░░░░░▐▌\n" +
            "▐▌░░░░░░░░░▐▀▀██▄░░░░░░▄▄▄░▐▌\n" +
            "─█░░░░░░░░░░░▀▀▀░░░░░░▀▀██░░█\n" +
            "─▐▌░░░░▄░░░░░░░░░░░░░▌░░░░░░█\n" +
            "──▐▌░░▐░░░░░░░░░░░░░░▀▄░░░░░█\n" +
            "───█░░░▌░░░░░░░░▐▀░░░░▄▀░░░▐▌\n" +
            "───▐▌░░▀▄░░░░░░░░▀░▀░▀▀░░░▄▀\n" +
            "───▐▌░░▐▀▄░░░░░░░░░░░░░░░░█\n" +
            "───▐▌░░░▌░▀▄░░░░▀▀▀▀▀▀░░░█\n" +
            "───█░░░▀░░░░▀▄░░░░░░░░░░▄▀\n" +
            "──▐▌░░░░░░░░░░▀▄░░░░░░▄▀\n" +
            "─▄▀░░░▄▀░░░░░░░░▀▀▀▀█▀\n" +
            "▀░░░▄▀░░░░░░░░░░▀░░░▀▀▀▀▄▄▄▄▄"

    init {
        this.name = "feels"
    }

    @Trace(operationName = "executeCommand", resourceName = "Feels")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply(feels)
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}
