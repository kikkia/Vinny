package com.bot.commands.meme

import com.bot.commands.MemeCommand
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
}
