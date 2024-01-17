package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class PauseCommand : VoiceCommand() {
    init {
        name = "pause"
        aliases = arrayOf("resume")
        arguments = ""
        help = "Pauses or resumes the stream"
    }

    @Trace(operationName = "executeCommand", resourceName = "pause")
    override fun executeCommand(commandEvent: CommandEvent) {
        val guildVoiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        if (!guildVoiceConnection.isConnected()) {
            commandEvent.replyWarning(" I am not currently connected to voice.")
            return
        }
        val newState = !guildVoiceConnection.getPaused()
        guildVoiceConnection.setPaused(newState)
        commandEvent.replySuccess("Stream has been ${if (newState) "paused" else "resumed"}")
    }
}