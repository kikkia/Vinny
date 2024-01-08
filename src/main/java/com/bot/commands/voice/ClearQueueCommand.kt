package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.bot.voice.GuildVoiceProvider
import com.bot.voice.VoiceSendHandler
import com.jagrosh.jdautilities.command.CommandEvent

class ClearQueueCommand : VoiceCommand() {
    init {
        this.name = "clearqueue"
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent!!.guild)
        if (!voiceConnection.isConnected()) {
            commandEvent.replyWarning("I am not connected to a voice channel.")
        } else {
            voiceConnection.clearQueue()
            commandEvent.reactSuccess()
        }
    }
}