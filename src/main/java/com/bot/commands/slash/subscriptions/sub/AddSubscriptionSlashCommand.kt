package com.bot.commands.slash.subscriptions.sub

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.RssDAO
import com.bot.exceptions.ChannelTypeNotSupportedException
import com.bot.exceptions.UsageLimitException
import com.bot.exceptions.UserPermissionsException
import com.bot.models.UsageLevel
import com.bot.utils.CommandCategories
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel

abstract class AddSubscriptionSlashCommand: BaseSlashCommand() {
    protected val rssDAO : RssDAO = RssDAO.getInstance()

    init {
        this.category = CommandCategories.SUBSCRIPTION
        this.botPermissions = arrayOf(Permission.MANAGE_WEBHOOKS)
    }

    override fun preExecute(command: ExtSlashCommandEvent) {
        if (!canAddSubscription(command)) {
            throw UsageLimitException("EXCEPTION_SUBSCRIPTION_LIMIT")
        }

        val subChannel = getEffectiveChannel(command)
        if (!command.member!!.hasPermission(subChannel, Permission.MANAGE_CHANNEL)) {
            throw UserPermissionsException("EXCEPTION_ADD_SUB_MISSING_PERMISSION", subChannel.asMention)
        }
    }

    private fun canAddSubscription(command: ExtSlashCommandEvent) : Boolean {
        val user = userDAO.getById(command.user.id)
        val usage = user?.usageLevel() ?: UsageLevel.BASIC
        return rssDAO.getCountForAuthor(command.user.id) < usage.maxSub
    }

    protected fun getEffectiveChannel(command: ExtSlashCommandEvent): GuildMessageChannel {
        val subChannel = command.optMessageChannel("channel") ?: command.channel.asGuildMessageChannel()
        if (subChannel.type != ChannelType.TEXT) {
            throw ChannelTypeNotSupportedException("SUBSCRIPTION_UNSUPPORTED_CHANNEL_TYPE")
        }
        // The above check filters out all non normal text channels, should be safe to get as guild channel now.
        return (command.optGuildChannel("channel") ?: command.channel.asGuildMessageChannel()) as GuildMessageChannel
    }
}