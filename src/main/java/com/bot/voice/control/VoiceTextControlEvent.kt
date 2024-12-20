package com.bot.voice.control

import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

class VoiceTextControlEvent(private val commandEvent: CommandEvent): VoiceControlEvent {
    override fun getMember(): Member {
        return commandEvent.member
    }

    override fun getChannel(): MessageChannel {
        return commandEvent.channel
    }

    override fun getAuthor(): User {
        return commandEvent.author
    }

    override fun getAuthorName(): String {
        return commandEvent.author.name
    }

    override fun getAuthorId(): String {
        return commandEvent.author.id
    }

    override fun getAuthorIdLong(): Long {
        return commandEvent.author.idLong
    }

    override fun getArgs(): String {
        return commandEvent.args
    }

    override fun getSource(): VoiceControlSource {
        return VoiceControlSource.TRADITIONAL
    }

    override fun sendMessage(msg: String): Message {
        return commandEvent.textChannel.sendMessage(msg).complete()
    }

    override fun reply(msg: String) {
        commandEvent.reply(msg)
    }

    override fun replySuccess(msg: String) {
        commandEvent.replySuccess(msg)
    }

    override fun replyWarning(msg: String) {
        commandEvent.replyWarning(msg)
    }

    override fun replyError(msg: String) {
        commandEvent.replyError(msg)
    }
}