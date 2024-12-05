package com.bot.commands.traditional

import com.bot.utils.CommandCategories
import net.dv8tion.jda.api.Permission

abstract class NSFWCommand : BaseCommand() {
    init {
        this.category = CommandCategories.NSFW
        this.guildOnly = false
        this.botPermissions = arrayOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS)
        this.canSchedule = true
    }
}
