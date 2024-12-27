package com.bot.commands.control

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

// This wrapper class allows for usage within the voice system which relies on the command (slash or trad) to make
// async callbacks to discord through them. This abstraction allows for both slash and trad commands to be handled.
interface CommandControlEvent {
    fun getMember(): Member
    fun getChannel(): MessageChannel
    fun getGuild(): Guild
    fun getAuthor(): User
    fun getAuthorName(): String
    fun getAuthorId(): String
    fun getAuthorIdLong(): Long
    fun getArgs(): String
    fun getSource(): CommandControlSource
    fun sendMessage(msg: String): Message
    fun reply(msg: String)
    fun replySuccess(msg: String)
    fun replyWarning(msg: String)
    fun replyError(msg: String)
}