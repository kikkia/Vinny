package com.bot.commands.slash

import com.bot.i18n.Translator
import com.jagrosh.jdautilities.command.CommandClient
import com.jagrosh.jdautilities.command.SlashCommandEvent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.components.actionrow.ActionRow

class ExtSlashCommandEvent(
    event: SlashCommandInteractionEvent,
    client: CommandClient
) : SlashCommandEvent(event, client) {
    private val translator = Translator.getInstance()

    fun replySuccessTranslated(outputId: String, vararg args: Any) {
        replyTranslatedMessage(successEmoji, outputId, *args)
    }

    fun replyWarningTranslated(outputId: String, vararg args: Any) {
        replyTranslatedMessage(warningEmoji, outputId, *args)
    }

    fun replyErrorTranslated(outputId: String, vararg args: Any) {
        replyTranslatedMessage(errorEmoji, outputId, *args)
    }

    fun replySuccess(msg: String) {
        replyMessage(successEmoji, msg)
    }

    fun replyWarning(msg: String) {
        replyMessage(warningEmoji, msg)
    }

    fun replyError(msg: String) {
        replyMessage(errorEmoji, msg)
    }

    fun replyWithActionBar(reply: String, actionBar: ActionRow) {
        this.hook.sendMessage(reply).setComponents(actionBar).queue()
    }

    fun replyToCommand(reply: String): Message {
        return this.hook.sendMessage(reply).complete()
    }
    
    fun replyTranslatedWithActionBar(outputId: String, actionBar: ActionRow, ephemeral: Boolean = false) {
        this.hook.sendMessage(translator.translate(outputId, this.userLocale.locale))
            .addComponents(actionBar)
            .setEphemeral(ephemeral)
            .queue()
    }

    fun replyGenericError() {
        replyTranslatedMessage(errorEmoji, "GENERIC_COMMAND_ERROR")
    }

    private fun replyTranslatedMessage(emoji: String, outputId: String, vararg args: Any) {
        this.hook.sendMessage(emoji + " " + translator.translate(outputId, this.userLocale.locale, *args)).complete()
    }

    private fun replyMessage(emoji: String, msg: String) {
        this.hook.sendMessage("$emoji $msg").complete()
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