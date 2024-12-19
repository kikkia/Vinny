package com.bot.commands.slash.subscriptions

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.RssDAO
import com.bot.models.RssChannelSubscription
import com.bot.utils.CommandCategories
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator
import java.sql.SQLException
import java.util.stream.Collectors

class MySubscriptionsSlashCommand(waiter: EventWaiter): BaseSlashCommand() {

    val rssDAO: RssDAO = RssDAO.getInstance()
    val builder: ButtonEmbedPaginator.Builder = ButtonEmbedPaginator.Builder()
    init {
        this.name = "mysubscriptions"
        this.help = "Lists your active subscriptions"
        this.category = CommandCategories.SUBSCRIPTION
        this.builder.setEventWaiter(waiter)
        this.builder.wrapPageEnds(true)
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        try {
            val subscriptions = rssDAO.getSubscriptionsForAuthor(command.user.id)

            if (subscriptions.isEmpty()) {
                command.replyWarning("LIST_SUBSCRIPTION_NONE_FOUND")
                return
            }

            val strings = subscriptions.stream().map { obj: RssChannelSubscription -> obj.toEmbed() }
                .collect(Collectors.toList())
            builder.setItems(strings)
            builder.setText("Subscriptions for ${command.user.asMention}")
            builder.build().paginate(command.hook, 1)
        } catch (throwable: SQLException) {
            logger.severe("Failed to get subs", throwable)
            throwable.printStackTrace()
            command.replyGenericError()
            return
        }
    }
}