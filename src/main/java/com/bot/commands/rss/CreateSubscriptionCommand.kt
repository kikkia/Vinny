package com.bot.commands.rss

import com.bot.commands.ModerationCommand
import com.bot.db.RssDAO
import com.bot.db.UserDAO
import com.bot.models.UsageLevel
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.commons.waiter.EventWaiter

abstract class CreateSubscriptionCommand : ModerationCommand() {
    protected val rssDAO : RssDAO = RssDAO.getInstance()
    private val userDAO : UserDAO = UserDAO.getInstance()
    protected lateinit var waiter : EventWaiter

    fun canMakeNewSubscription(commandEvent: CommandEvent) : Boolean {
        val user = userDAO.getById(commandEvent.author.id)
        val usage = user?.usageLevel() ?: UsageLevel.BASIC
        if (rssDAO.getCountForAuthor(commandEvent.author.id) >= usage.maxSub) {
            commandEvent.replyWarning("You can only make ${usage.maxSub} subscriptions." +
                    "To be able to make more, you can subscribe on the Vinny support server store." +
                    " To get a support server invite use `~support`.\nYou can also remove your current subscriptions with the " +
                            "`~unsubscribe` command")
            return false
        }
        return true
    }
}