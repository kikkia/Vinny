package com.bot.interactions.commands.voice

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.VoiceCommand
import com.bot.utils.VoiceUtils.Companion.parseSeekPos
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class SeekCommand : VoiceCommand() {
    init {
        name = "seek"
        arguments = "Position (e.g. 1:23 -> 1 minute and 23 seconds)"
        help = "Seeks to a position in a track"
    }

    @Trace(operationName = "executeCommand", resourceName = "Seek")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        if (!voiceConnection.isConnected()) {
            commandEvent.reply(commandEvent.client.warning + " I am not currently connected to voice.")
            return
        }
        if (voiceConnection.nowPlaying() == null) {
            commandEvent.reply(commandEvent.client.warning + " I am not currently playing anything")
            return
        }

        val seekPos = parseSeekPos(commandEvent.args)

        voiceConnection.seek(seekPos, voiceConnection.nowPlaying()!!)
        commandEvent.reactSuccess()
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}