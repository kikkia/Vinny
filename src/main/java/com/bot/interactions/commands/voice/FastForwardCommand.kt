package com.bot.interactions.commands.voice

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.VoiceCommand
import com.bot.utils.VoiceUtils.Companion.parseSeekPos
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class FastForwardCommand : VoiceCommand() {
    init {
        name = "fastforward"
        aliases = arrayOf("ff", "fforward")
        arguments = "Duration to fast forward (e.g. 1:23 -> 1 minute and 23 seconds)"
        help = "fast forwards a given duration in the now playing track"
    }

    @Trace(operationName = "executeCommand", resourceName = "FastForward")
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
        val ffDur: Long = if (commandEvent.args.isEmpty()) {
            9999999999L
        } else {
            parseSeekPos(commandEvent.args)
        }
        val newPos = voiceConnection.getPosition()!! + ffDur
        if (newPos >= voiceConnection.nowPlaying()!!.track.info.length) {
            voiceConnection.nextTrack(true)
        } else {
            voiceConnection.seek(newPos, voiceConnection.nowPlaying()!!)
        }
        commandEvent.reactSuccess()
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}