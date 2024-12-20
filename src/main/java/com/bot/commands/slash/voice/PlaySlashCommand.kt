package com.bot.commands.slash.voice

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent

class PlaySlashCommand: BaseSlashCommand() {

    init {
        this.name = "play"
        this.help = "Play a track in voice"
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        TODO("Not yet implemented")
    }
}