package com.bot.interactions.commands.voice

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.VoiceCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class SkipCommand : VoiceCommand() {
    init {
        name = "skip"
        arguments = ""
        help = "skips to the next track"
    }

    @Trace(operationName = "executeCommand", resourceName = "SkipTrack")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        if (!voiceConnection.isConnected()) {
            commandEvent.reply(commandEvent.client.warning + " I am not connected to a voice channel.")
            return
        }
        voiceConnection.nextTrack(true)
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}