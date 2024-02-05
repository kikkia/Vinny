package com.bot.interactions

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

class SlashCommandInteraction(val event: SlashCommandEvent) : InteractionEvent {
    override fun ack() {
        event.deferReply().queue()
    }

    override fun reply(msg: String) {
        event.reply(msg).queue()
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
        return event.options.toString()
    }
}