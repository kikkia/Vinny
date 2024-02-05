package com.bot.interactions.commands.meme

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.MemeCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class LennyCommand : MemeCommand() {

    private val lenny = "───█───▄▀█▀▀█▀▄▄───▐█──────▄▀█▀▀█▀▄▄\n" +
            "──█───▀─▐▌──▐▌─▀▀──▐█─────▀─▐▌──▐▌─█▀\n" +
            "─▐▌──────▀▄▄▀──────▐█▄▄──────▀▄▄▀──▐▌\n" +
            "─█────────────────────▀█────────────█\n" +
            "▐█─────────────────────█▌───────────█\n" +
            "▐█─────────────────────█▌───────────█\n" +
            "─█───────────────█▄───▄█────────────█\n" +
            "─▐▌───────────────▀███▀────────────▐▌\n" +
            "──█──────────▀▄───────────▄▀───────█\n" +
            "───█───────────▀▄▄▄▄▄▄▄▄▄▀────────█"

    init {
        this.name = "lenny"
        this.help = "prints a lenny face"
    }

    @Trace(operationName = "executeCommand", resourceName = "Lenny")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply(lenny)
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}
