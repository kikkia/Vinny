package com.bot.commands

import com.bot.utils.CommandCategories
import net.dv8tion.jda.api.Permission

abstract class ModerationCommand : BaseCommand() {
    init {
        this.category = CommandCategories.MODERATION
        this.guildOnly = true
        this.botPermissions = arrayOf(Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION)
    }
}
