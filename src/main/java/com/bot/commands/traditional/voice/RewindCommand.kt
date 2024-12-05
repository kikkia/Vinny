package com.bot.commands.traditional.voice

import com.bot.commands.traditional.VoiceCommand
import com.bot.utils.VoiceUtils.Companion.parseSeekPos
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class RewindCommand : VoiceCommand() {
    init {
        name = "rewind"
        aliases = arrayOf("rw")
        arguments = "Duration to rewind (e.g. 1:23 -> 1 minute and 23 seconds)"
        help = "rewinds a given duration backwards in the now playing track"
    }

    @Trace(operationName = "executeCommand", resourceName = "Rewind")
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
        val rwDur: Long = if (commandEvent.args.isEmpty()) {
            Long.MAX_VALUE
        } else {
            parseSeekPos(commandEvent.args)
        }

        val currentPos = voiceConnection.getPosition()

        voiceConnection.seek((currentPos!! - rwDur).coerceAtLeast(0), voiceConnection.nowPlaying()!!)
        commandEvent.reactSuccess()
    }
}