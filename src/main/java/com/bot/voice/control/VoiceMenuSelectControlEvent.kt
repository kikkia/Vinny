package com.bot.voice.control

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

class VoiceMenuSelectControlEvent(private val event: StringSelectInteractionEvent): VoiceControlEvent {
    override fun getMember(): Member {
        return event.member!!
    }

    override fun getChannel(): MessageChannel {
        return event.channel
    }

    override fun getAuthor(): User {
        return event.user
    }

    override fun getAuthorName(): String {
        return event.user.name
    }

    override fun getAuthorId(): String {
        return event.user.id
    }

    override fun getAuthorIdLong(): Long {
        return event.user.idLong
    }

    override fun getArgs(): String {
        TODO("Not yet implemented")
    }

    override fun getSource(): VoiceControlSource {
        return VoiceControlSource.MENU
    }

    override fun sendMessage(msg: String): Message {
        return event.hook.editOriginal(msg).complete()
    }

    override fun reply(msg: String) {
        event.hook.editOriginal(msg).complete()
    }

    override fun replySuccess(msg: String) {
        event.hook.editOriginal(msg).complete()
    }

    override fun replyWarning(msg: String) {
        event.hook.editOriginal(msg).complete()
    }

    override fun replyError(msg: String) {
        event.hook.editOriginal(msg).complete()
    }
}