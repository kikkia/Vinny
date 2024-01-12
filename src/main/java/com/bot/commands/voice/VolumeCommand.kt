package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class VolumeCommand : VoiceCommand() {
    init {
        name = "volume"
        arguments = "<Volume 1-200>"
        help = "Sets the players volume"
    }

    @Trace(operationName = "executeCommand", resourceName = "Volume")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        val newVolume: Int
        try {
            if (!voiceConnection.isConnected()) {
                commandEvent.replyWarning("I am not connected to a voice channel.")
                return
            }
            newVolume = commandEvent.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toInt()
            voiceConnection.setVolume(newVolume)
            commandEvent.reactSuccess()
        } catch (e: NumberFormatException) {
            commandEvent.replyWarning(
                """
    You can enter a volume between 0 and 200 to set.
    Current volume: ${voiceConnection.getVolume()}
    """.trimIndent()
            )
        }
    }
}