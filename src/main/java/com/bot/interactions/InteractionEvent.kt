package com.bot.interactions

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User

interface InteractionEvent {

    // Acknowledge the event was received
    fun ack()

    // Replies a message to the event in the channel received
    fun reply(msg: String)

    fun replyWarning(msg: String)

    fun replyError(msg:String)

    fun getChannel() : TextChannel

    fun getMember() : Member?

    fun getUser() : User

    fun getGuild() : Guild

    fun getArgs() : String
}