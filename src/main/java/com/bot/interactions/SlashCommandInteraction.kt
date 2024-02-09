package com.bot.interactions

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import java.util.function.Consumer

class SlashCommandInteraction(val event: SlashCommandEvent) : InteractionEvent {

    private var deferred = false
    override fun ack() {
        event.deferReply().queue()
        deferred = true
    }

    private fun sendMessage(msg: String) {
        if (deferred) {
            event.hook.sendMessage(msg).queue()
        } else {
            event.reply(msg).queue()
        }
    }

    private fun sendMessage(msg: String, success: Consumer<Message>) {
        val action = if (deferred) {
            event.hook.sendMessage(msg)
        } else {
            event.reply(msg)
        }
        action.queue {
            success.accept(it as Message)
        }
    }

    override fun reply(msg: String) {
        sendMessage(msg)
    }

    override fun reply(msg: String, success: Consumer<Message>) {
        sendMessage(msg, success)
    }

    override fun replyWarning(msg: String) {
       sendMessage("❗ $msg")
    }

    override fun replyError(msg: String) {
        sendMessage("❌ $msg")
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

    override fun getSelfMember(): Member {
        return event.member!!
    }

    override fun isFromType(type: ChannelType): Boolean {
        return event.channelType == type
    }
}