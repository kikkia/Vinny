package com.bot.commands.traditional.voice

import com.bot.commands.control.TextControlEvent
import com.bot.commands.traditional.VoiceCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class LofiCommand : VoiceCommand() {
    init {
        name = "lofi"
        help = "Starts playing a lofi music radio station"
    }

    @Trace(operationName = "executeCommand", resourceName = "lofi")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)

        voiceConnection.setRadio(TextControlEvent(commandEvent))
        commandEvent.reactSuccess()
    }
}