package com.bot.interactions.commands.voice

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.VoiceCommand
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

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}