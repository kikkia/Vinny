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
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val id = command.optLong("id").toInt()
        val subscription: RssChannelSubscription?
        val channel: InternalChannel
        try {
            subscription = rssDAO.getChannelSubById(id)
            channel = channelDAO.getTextChannelForId(subscription.channel)
        } catch (e: SQLException) {
            command.replyError("Failed to remove subscription")
            logger.severe("Failed to get sub or channel from db", e)
            return
        }
        if (subscription == null) {
            command.replyWarning("Failed to find subscription")
            return
        }

        if (channel.guildId != command.guild!!.id) {
            command.replyWarning("You cannot remove this subscription unless you are in the guild it is in.")
            return
        }

        if (command.user.id != subscription.author &&
            !command.member!!.hasPermission(command.guild!!.getGuildChannelById(subscription.channel.toLong())!!, Permission.MANAGE_CHANNEL)) {
            command.replyWarning("You cannot remove this command unless it is yours OR you have " +
                    "the MANAGE_CHANNEL permission for the channel it is in.")
            return
        }

        try {
            rssDAO.removeChannelSubscription(subscription)
            command.replySuccess(translator.translate("greeting", command.userLocale.locale, command.user.name))
        } catch (e: SQLException) {
            logger.severe("Failed to remove channel sub", e)
            command.replyError("Failed to remove the subscription")
        }
    }
}