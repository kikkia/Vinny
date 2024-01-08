package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.bot.voice.VoiceSendHandler
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class ShuffleCommand : VoiceCommand() {
    init {
        name = "shuffle"
        help = "Shuffles the current voice queue"
    }

    @Trace(operationName = "executeCommand", resourceName = "Shuffle")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        try {
            voiceConnection.shuffleTracks()
            commandEvent.reactSuccess()
        } catch (e: Exception) {
            commandEvent.replyError("Failed to shuffle tracks")
            logger.severe("Failed to shuffle tracks", e)
        }
    }
}