package com.bot.commands.slash.nsfw

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.exceptions.newstyle.ChannelTypeNotSupportedException
import com.bot.exceptions.newstyle.NSFWNotAllowedException
import com.bot.utils.CommandCategories
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType

abstract class NsfwSlashCommand: BaseSlashCommand() {
    init {
        this.category = CommandCategories.NSFW
        this.botPermissions = arrayOf(Permission.MANAGE_WEBHOOKS)
    }

    override fun preExecute(command: ExtSlashCommandEvent) {
        if (command.channel.type != ChannelType.TEXT) {
            throw ChannelTypeNotSupportedException("COMMAND_UNSUPPORTED_CHANNEL_TYPE")
        }
        if (!command.guildChannel.asTextChannel().isNSFW) {
            throw NSFWNotAllowedException("NSFW_NOT_ALLOWED_EXCEPTION")
        }
    }
}