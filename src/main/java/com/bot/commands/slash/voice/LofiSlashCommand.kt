package com.bot.commands.slash.voice

import com.bot.commands.control.SlashControlEvent
import com.bot.commands.slash.ExtSlashCommandEvent

class LofiSlashCommand: VoiceSlashCommand() {

    init {
        this.name = "lofi"
        this.help = "Play a lofi radio station"
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        provider.getGuildVoiceConnection(command.guild!!).setRadio(SlashControlEvent(command))
    }
}