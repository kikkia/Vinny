package com.bot.interactions.commands

import com.bot.utils.CommandCategories

abstract class OwnerCommand : BaseCommandText() {
    init {
        this.ownerCommand = true
        this.guildOnly = false
        this.hidden = true
        this.category = CommandCategories.OWNER
        this.canSchedule = false
    }
}
