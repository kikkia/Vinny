package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.bot.models.enums.RepeatMode
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class RepeatCommand : VoiceCommand() {
    init {
        name = "repeat"
        arguments = "`all` for repeat all, no input for repeat one"
        help = "Toggles repeating the current track or all based on input"
    }

    @Trace(operationName = "executeCommand", resourceName = "Repeat")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        if (!voiceConnection.isConnected()) {
            commandEvent.reply(commandEvent.client.warning + " I am not currently connected to voice.")
            return
        }
        var mode = RepeatMode.REPEAT_NONE
        if (commandEvent.args.equals("all", ignoreCase = true)) {
           mode = RepeatMode.REPEAT_ALL
        } else if (commandEvent.args.equals("one", ignoreCase = false)) {
            mode = RepeatMode.REPEAT_ONE
        }
        voiceConnection.setRepeatMode(mode)
        commandEvent.reply("${commandEvent.client.success} Set the repeat mode to ${mode.ezName}")

    }
}