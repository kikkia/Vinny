package com.bot.commands.slash

import com.bot.i18n.Translator
import com.jagrosh.jdautilities.command.CommandClient
import com.jagrosh.jdautilities.command.SlashCommandEvent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ItemComponent

class ExtSlashCommandEvent(
    event: SlashCommandInteractionEvent,
    client: CommandClient
) : SlashCommandEvent(event, client) {
    private val translator = Translator.getInstance()

    fun replySuccess(outputId: String, vararg args: Any) {
        replyTranslatedMessage(successEmoji, outputId, *args)
    }

    fun replyWarning(outputId: String, vararg args: Any) {
        replyTranslatedMessage(warningEmoji, outputId, *args)
    }

    fun replyError(outputId: String, vararg args: Any) {
        replyTranslatedMessage(errorEmoji, outputId, *args)
    }

    fun replyWithActionBar(reply: String, actionBar: MutableCollection<ItemComponent>) {
        this.hook.sendMessage(reply).addActionRow(actionBar).queue()
    }

    fun replyToCommand(reply: String): Message {
        return this.hook.sendMessage(reply).complete()
    }
    
    fun replyTranslatedWithActionBar(outputId: String, actionBar: MutableCollection<ItemComponent>, ephemeral: Boolean = false) {
        this.hook.sendMessage(translator.translate(outputId, this.userLocale.locale))
            .setActionRow(actionBar)
            .setEphemeral(ephemeral)
            .queue()
    }

    fun replyGenericError() {
        replyTranslatedMessage(errorEmoji, "GENERIC_COMMAND_ERROR")
    }

    private fun replyTranslatedMessage(emoji: String, outputId: String, vararg args: Any) {
        this.hook.sendMessage(emoji + " " + translator.translate(outputId, this.userLocale.locale, *args)).queue()
    }

    companion object {
        const val successEmoji = "✅"
        const val warningEmoji = "❗"
        const val errorEmoji = "❌"
        fun fromCommandEvent(event: SlashCommandEvent): ExtSlashCommandEvent {
            return ExtSlashCommandEvent(event, event.client)
        }
    }
}