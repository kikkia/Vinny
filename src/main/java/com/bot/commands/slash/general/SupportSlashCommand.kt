package com.bot.commands.slash.general

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.utils.CommandCategories

class SupportSlashCommand: BaseSlashCommand() {

    init {
        this.name = "support"
        this.help = "Posts an inivte to the support server."
        this.category = CommandCategories.GENERAL
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        command.replyToCommand(translator.translate("SUPPORT_INVITE_LINK", command.userLocale.locale))
    }
}