package com.bot.commands.slash.subscriptions

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.ChannelDAO
import com.bot.db.RssDAO
import com.bot.models.InternalChannel
import com.bot.models.RssChannelSubscription
import com.bot.utils.CommandCategories
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.sql.SQLException

class UnsubscribeSlashCommand: BaseSlashCommand() {

    val rssDAO: RssDAO = RssDAO.getInstance()
    private val channelDAO: ChannelDAO = ChannelDAO.getInstance()

    init {
        this.name = "unsubscribe"
        this.help = "Removes a subscription by id"
        this.category = CommandCategories.SUBSCRIPTION
        this.options = listOf(OptionData(OptionType.INTEGER, "id", "id of the sub to remove", true))
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val id = command.optLong("id").toInt()
        val subscription: RssChannelSubscription?
        val channel: InternalChannel
        try {
            subscription = rssDAO.getChannelSubById(id)
            if (subscription == null) {
                command.replyWarningTranslated("REMOVE_SUBSCRIPTION_NOT_FOUND")
                return
            }
            channel = channelDAO.getTextChannelForId(subscription.channel)
        } catch (e: SQLException) {
            command.replyErrorTranslated("GENERIC_COMMAND_ERROR")
            logger.severe("Failed to get sub or channel from db", e)
            return
        }

        if (channel.guildId != command.guild!!.id) {
            command.replyWarningTranslated("REMOVE_SUBSCRIPTION_DIFF_GUILD")
            return
        }

        if (command.user.id != subscription.author &&
            !command.member!!.hasPermission(command.guild!!.getGuildChannelById(subscription.channel.toLong())!!, Permission.MANAGE_CHANNEL)) {
            command.replyWarningTranslated("REMOVE_SUBSCRIPTION_PERMISSION")
            return
        }

        try {
            rssDAO.removeChannelSubscription(subscription)
            command.replySuccessTranslated("REMOVE_SUBSCRIPTION_SUCCESS")
        } catch (e: SQLException) {
            logger.severe("Failed to remove channel sub", e)
            command.replyErrorTranslated("Failed to remove the subscription")
        }
    }
}