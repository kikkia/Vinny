package com.bot.interactions.commands

import com.bot.utils.CommandCategories

abstract class MemeCommand : BaseCommandText() {
    init {
        this.category = CommandCategories.MEME
        this.canSchedule = true
    }
}
