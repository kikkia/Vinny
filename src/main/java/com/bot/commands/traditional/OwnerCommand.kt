package com.bot.commands.traditional

import com.bot.utils.CommandCategories

abstract class OwnerCommand : BaseCommand() {
    init {
        this.ownerCommand = true
        this.guildOnly = false
        this.hidden = true
        this.category = CommandCategories.OWNER
        this.canSchedule = false
    }
}
