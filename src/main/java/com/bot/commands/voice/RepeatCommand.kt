package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.bot.models.enums.RepeatMode
import com.bot.voice.GuildVoiceProvider
import com.bot.voice.VoiceSendHandler
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class RepeatCommand : VoiceCommand() {
    val guildVoiceProvider = GuildVoiceProvider.getInstance()
    init {
        name = "repeat"
        arguments = "`all` for repeat all, no input for repeat one"
        help = "Toggles repeating the current track or all based on input"
    }

    @Trace(operationName = "executeCommand", resourceName = "Repeat")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceProvider = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild.idLong)
        if (voiceProvider == null) {
            commandEvent.reply(commandEvent.client.warning + " I am not currently connected to voice.")
        } else {
            var mode = RepeatMode.REPEAT_NONE
            if (commandEvent.args.equals("all", ignoreCase = true)) {
               mode = RepeatMode.REPEAT_ALL
            } else if (commandEvent.args.equals("one", ignoreCase = false)) {
                mode = RepeatMode.REPEAT_ONE
            }
            voiceProvider.trackProvider.setRepeatMode(mode)
            commandEvent.reply("${commandEvent.client.success} Set the repeat mode to ${mode.ezName}")
        }
    }
}