package com.bot.commands.slash.general

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.utils.CommandCategories

class InviteSlashCommand: BaseSlashCommand() {

    init {
        this.name = "invite"
        this.help = "Invite Vinny to your server."
        this.category = CommandCategories.GENERAL
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        command.replyToCommand("https://discord.com/oauth2/authorize?client_id=276855867796881408&scope=bot+applications.commands&permissions=277796481399")
    }
}