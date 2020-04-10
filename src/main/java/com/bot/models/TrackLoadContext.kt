package com.bot.models

import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel

class TrackLoadContext(val trackQueryString: String,
                       val textChannel: TextChannel,
                       val event: CommandEvent,
                       val progressMessage: Message) {
    fun replyError(message: String) {
        progressMessage.editMessage(event.client.error + " " + message).queue()
    }

    fun replyWarn(message: String) {
        progressMessage.editMessage(event.client.warning + " " + message).queue()
    }

    fun replySuccess(message: String) {
        progressMessage.editMessage(event.client.success + " " + message).queue()
    }

    fun replyEmbed(embed: MessageEmbed) {
        progressMessage.editMessage(embed).queue()
    }
}