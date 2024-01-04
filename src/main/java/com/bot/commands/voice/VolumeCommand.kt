package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.bot.voice.LavaLinkClient
import com.bot.voice.VoiceSendHandler
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class VolumeCommand : VoiceCommand() {
    private val client: LavaLinkClient

    init {
        name = "volume"
        arguments = "<Volume 1-200>"
        help = "Sets the players volume"
        client = LavaLinkClient.getInstance()
    }

    @Trace(operationName = "executeCommand", resourceName = "Volume")
    override fun executeCommand(commandEvent: CommandEvent) {
        val handler = commandEvent.guild.audioManager.sendingHandler as VoiceSendHandler?
        val newVolume: Int
        try {
            if (handler == null) {
                commandEvent.replyWarning("I am not connected to a voice channel.")
                return
            }
            newVolume = commandEvent.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toInt()
            if (newVolume > 200 || newVolume < 0) {
                throw NumberFormatException()
            }
            if (handler.isLocked) {
                commandEvent.replyWarning("Volume and speed is currently locked. You need to unlock it to edit it.")
                return
            }
            handler.player.volume = newVolume
            commandEvent.reactSuccess()
        } catch (e: NumberFormatException) {
            commandEvent.replyWarning(
                """
    You can enter a volume between 0 and 200 to set.
    Current volume: ${handler!!.player.volume}
    """.trimIndent()
            )
        }
    }
}