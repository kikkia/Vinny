package com.bot.commands

import com.bot.utils.CommandCategories

abstract class MemeCommand : BaseCommand() {
    init {
        this.category = CommandCategories.MEME
    }
}
