package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.bot.voice.GuildVoiceProvider
import com.bot.voice.VoiceSendHandler
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class StopCommand : VoiceCommand() {
    val guildVoiceProvider = GuildVoiceProvider.getInstance()
    init {
        name = "stop"
        arguments = ""
        help = "Stops stream and clears the current playlist"
    }

    @Trace(operationName = "executeCommand", resourceName = "Stop")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild.idLong)
        if (voiceConnection == null) {
            commandEvent.reply(commandEvent.client.warning + " I am not connected to a voice channel.")
        } else {
            voiceConnection.cleanupPlayer()
            commandEvent.reply(commandEvent.client.success + " Stopped audio stream")
            commandEvent.guild.audioManager.closeAudioConnection()
        }
    }
}