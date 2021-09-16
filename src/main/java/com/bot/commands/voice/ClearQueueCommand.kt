package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.bot.voice.VoiceSendHandler
import com.jagrosh.jdautilities.command.CommandEvent
import org.springframework.stereotype.Component

@Component
class ClearQueueCommand : VoiceCommand() {

    init {
        this.name = "clearqueue"
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        val handler = commandEvent!!.guild.audioManager.sendingHandler as VoiceSendHandler?
        if (handler == null) {
            commandEvent.replyWarning("I am not connected to a voice channel.")
        } else {
            handler.clearQueue()
            commandEvent.reactSuccess()
        }
    }
}