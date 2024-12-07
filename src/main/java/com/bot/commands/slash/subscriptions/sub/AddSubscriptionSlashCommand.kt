package com.bot.commands.slash.subscriptions.sub

import com.bot.commands.slash.BaseSlashCommand
import com.bot.db.RssDAO
import com.bot.utils.CommandCategories

abstract class AddSubscriptionSlashCommand: BaseSlashCommand() {
    protected val rssDAO : RssDAO = RssDAO.getInstance()

    init {
        this.category = CommandCategories.SUBSCRIPTION
    }
}