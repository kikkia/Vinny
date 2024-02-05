package com.bot.interactions.commands

import com.bot.utils.CommandCategories
import net.dv8tion.jda.api.Permission

abstract class GeneralCommand : BaseCommandText() {
    init {
        this.category = CommandCategories.GENERAL
        this.guildOnly = true
        this.ownerCommand = false
        this.hidden = false
        this.botPermissions = arrayOf(Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION)
        this.canSchedule = true
    }
}
