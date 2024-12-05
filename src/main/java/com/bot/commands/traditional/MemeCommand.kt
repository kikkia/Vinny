package com.bot.commands.traditional

import com.bot.utils.CommandCategories

abstract class MemeCommand : BaseCommand() {
    init {
        this.category = CommandCategories.MEME
        this.canSchedule = true
    }
}
