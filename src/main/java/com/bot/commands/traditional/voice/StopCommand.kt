package com.bot.commands.traditional.voice

import com.bot.commands.traditional.VoiceCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class StopCommand : VoiceCommand() {
    init {
        name = "stop"
        arguments = ""
        help = "Stops stream and clears the current playlist"
    }

    @Trace(operationName = "executeCommand", resourceName = "Stop")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        if (!voiceConnection.isConnected()) {
            commandEvent.reply(commandEvent.client.warning + " I am not connected to a voice channel.")
        } else {
            voiceConnection.cleanupPlayer()
            commandEvent.reply(commandEvent.client.success + " Stopped audio stream")
        }
    }
}