package com.bot.interactions.commands.voice

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.ModerationCommand
import com.bot.voice.GuildVoiceProvider
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class LockVolumeCommand : ModerationCommand() {
    private val guildVoiceProvider = GuildVoiceProvider.getInstance()
    init {
        name = "lockvolume"
        aliases = arrayOf("lvolume", "lvol", "lockv")
        help = "Locks the volume for the playing stream. (Mod required)"
    }

    @Trace(operationName = "executeCommand", resourceName = "LockVolume")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        if (!voiceConnection.isConnected()) {
            commandEvent.reply(commandEvent.client.warning + " I am not currently playing audio.")
            return
        }
        val message = if (voiceConnection.toggleVolumeLock()) "Volume is now locked" else "Volume is now unlocked"
        commandEvent.replySuccess(message)
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}