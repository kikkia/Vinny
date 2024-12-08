package com.bot.commands.slash

import com.jagrosh.jdautilities.command.CommandClient
import com.jagrosh.jdautilities.command.SlashCommandEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ExtSlashCommandEvent(
    event: SlashCommandInteractionEvent,
    client: CommandClient
) : SlashCommandEvent(event, client) {

    fun replySuccess(content: String) {
        this.hook.sendMessage(successEmoji + content).queue()
    }

    fun replyWarning(content: String) {
        this.hook.sendMessage(warningEmoji + content).queue()
    }

    fun replyError(content: String) {
        this.hook.sendMessage(errorEmoji + content).queue()
    }

    companion object {
        val successEmoji = "✅"
        val warningEmoji = "❗"
        val errorEmoji = "❌"
        fun fromCommandEvent(event: SlashCommandEvent): ExtSlashCommandEvent {
            return ExtSlashCommandEvent(event, event.client)
        }
    }
}