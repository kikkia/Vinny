package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.bot.voice.GuildVoiceProvider
import com.bot.voice.LavaLinkClient
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import dev.arbjerg.lavalink.client.LavalinkPlayer

class PauseCommand : VoiceCommand() {
    private val guildVoiceProvider: GuildVoiceProvider

    init {
        name = "pause"
        aliases = arrayOf("resume")
        arguments = ""
        help = "Pauses or resumes the stream"
        guildVoiceProvider = GuildVoiceProvider.getInstance()
    }

    @Trace(operationName = "executeCommand", resourceName = "pause")
    override fun executeCommand(commandEvent: CommandEvent) {
        val guildVoiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        if (!guildVoiceConnection.isConnected()) {
            commandEvent.replyWarning(" I am not currently connected to voice.")
            return
        }
        guildVoiceConnection.setPaused(!guildVoiceConnection.getPaused())
        commandEvent.replySuccess("Stream has been ${if (guildVoiceConnection.getPaused()) "paused" else "resumed"}")
    }
}