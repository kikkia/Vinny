package com.bot.interactions

import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.*
import java.util.function.Consumer

class TextCommandInteraction(val event: CommandEvent) : InteractionEvent {
    override fun ack() {
        event.channel.sendTyping().queue()
    }

    override fun reply(msg: String) {
        event.reply(msg)
    }

    override fun reply(msg: String, success: Consumer<Message>) {
        event.reply(msg, success)
    }

    override fun replyWarning(msg: String) {
        event.replyWarning(msg)
    }

    override fun replyError(msg: String) {
        event.replyError(msg)
    }

    override fun getChannel(): TextChannel {
        return event.textChannel
    }

    override fun getMember(): Member? {
        return event.member
    }

    override fun getUser(): User {
        return event.author
    }

    override fun getGuild(): Guild {
        return event.guild
    }

    override fun getArgs(): String {
        return event.args
    }

    override fun getSelfMember(): Member {
        return event.selfMember
    }

    override fun isFromType(type: ChannelType): Boolean {
        return event.channelType == type
    }
}