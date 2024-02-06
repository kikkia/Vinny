package com.bot.interactions

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

class SlashCommandInteraction(val event: SlashCommandEvent) : InteractionEvent {

    var deferred = false
    override fun ack() {
        event.deferReply().queue()
        deferred = true
    }

    override fun reply(msg: String) {
        if (deferred) {
            event.hook.sendMessage(msg).queue()
        } else {
            event.reply(msg).queue()
        }
    }

    override fun replyWarning(msg: String) {
        // TODO
        event.reply(msg).queue()
    }

    override fun replyError(msg: String) {
        // TODO
        event.reply(msg).queue()
    }

    override fun getChannel(): TextChannel {
        return event.textChannel
    }

    override fun getMember(): Member? {
        return event.member
    }

    override fun getUser(): User {
        return event.user
    }

    override fun getGuild(): Guild {
        return event.guild!!
    }

    override fun getArgs(): String {
        // TODO
        val sb = StringBuilder()
        for (opt in event.options) {
            sb.append("${opt.asString} ")
        }
        return sb.toString()
    }

    override fun isFromType(type: ChannelType): Boolean {
        return event.channelType == type
    }
}