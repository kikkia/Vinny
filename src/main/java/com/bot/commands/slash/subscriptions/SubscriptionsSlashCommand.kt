package com.bot.commands.slash.subscriptions

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.RssDAO
import com.bot.models.RssChannelSubscription
import com.bot.utils.CommandCategories
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.sql.SQLException
import java.util.stream.Collectors

class SubscriptionsSlashCommand(waiter: EventWaiter) : BaseSlashCommand() {

    val rssDAO: RssDAO = RssDAO.getInstance()
    val builder: ButtonEmbedPaginator.Builder = ButtonEmbedPaginator.Builder()
    init {
        this.name = "subscriptions"
        this.help = "Lists active subscriptions"
        this.options = listOf(
            OptionData(OptionType.CHANNEL, "channel", "The channel to list subs for", false),
            OptionData(OptionType.BOOLEAN, "whole-guild", "Get subs for the whole guild", false)
        )
        this.category = CommandCategories.SUBSCRIPTION
        this.builder.setEventWaiter(waiter)
        this.builder.wrapPageEnds(true)
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val effectiveChannel = command.optMessageChannel("channel") ?: command.channel
        val wholeGuild = command.optBoolean("whole-guild")

        try {
            val subscriptions = if (wholeGuild) rssDAO.getSubscriptionsForGuild(command.guild!!.id)
                else rssDAO.getSubscriptionsForChannel(effectiveChannel.id)

            if (subscriptions.isEmpty()) {
                command.replyWarning("LIST_SUBSCRIPTION_NONE_FOUND")
                return
            }

            val strings = subscriptions.stream().map { obj: RssChannelSubscription -> obj.toEmbed() }
                .collect(Collectors.toList())
            builder.setItems(strings)
            val context = if (wholeGuild) command.guild!!.name else effectiveChannel.name
            builder.setText("Subscriptions in $context")

            builder.build().paginate(command.hook, 1)
        } catch (throwable: SQLException) {
            logger.severe("Failed to get subs", throwable)
            throwable.printStackTrace()
            command.replyGenericError()
            return
        }
    }
}