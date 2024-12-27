package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent

class LogoutSlashCommand: VoiceSlashCommand(false) {
    init {
        this.name = "logout"
        this.help = "Removes voice account you have logged in"
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        TODO("Not yet implemented")
    }
}