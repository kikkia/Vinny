package com.bot.commands.control

import com.bot.commands.slash.ExtSlashCommandEvent
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

class SlashControlEvent(private val command: ExtSlashCommandEvent): CommandControlEvent {
    override fun getMember(): Member {
        return command.member!!
    }

    override fun getChannel(): MessageChannel {
        return command.channel
    }

    override fun getGuild(): Guild {
        return command.guild!!
    }

    override fun getAuthor(): User {
        return command.user
    }

    override fun getAuthorName(): String {
        return command.user.name
    }

    override fun getAuthorId(): String {
        return command.user.id
    }

    override fun getAuthorIdLong(): Long {
        return command.user.idLong
    }

    override fun getArgs(): String {
        return ""
    }

    override fun getSource(): CommandControlSource {
        return CommandControlSource.SLASH
    }

    override fun sendMessage(msg: String): Message {
        return command.replyToCommand(msg)
    }

    override fun reply(msg: String) {
        command.replyToCommand(msg)
    }

    override fun replySuccess(msg: String) {
        command.replySuccess(msg)
    }

    override fun replyWarning(msg: String) {
        command.replyWarning(msg)
    }

    override fun replyError(msg: String) {
        command.replyError(msg)
    }
}