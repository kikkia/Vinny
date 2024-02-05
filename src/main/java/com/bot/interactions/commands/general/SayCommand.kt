package com.bot.interactions.commands.general

import club.minnced.discord.webhook.send.WebhookMessage
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.bot.interactions.commands.GeneralCommand
import com.bot.exceptions.ScheduledCommandFailedException
import com.bot.interactions.InteractionEvent
import com.bot.utils.FormattingUtils
import com.bot.utils.ScheduledCommandUtils
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class SayCommand : GeneralCommand() {
    init {
        this.name = "say"
        this.help = "Repeat after you"
        this.arguments = "<Something to say>"
    }

    @Trace(operationName = "executeCommand", resourceName = "Say")
    override fun executeCommand(commandEvent: CommandEvent) {
        if (ScheduledCommandUtils.isScheduled(commandEvent)) {
            try {
                val webhook = ScheduledCommandUtils.getWebhookForChannel(commandEvent)
                webhook.send(buildWebhookMessage(commandEvent))
            } catch (e : ScheduledCommandFailedException) {
                commandEvent.replyWarning(e.message)
            }
        } else {
            commandEvent.reply(FormattingUtils.cleanSayCommand(commandEvent))
        }
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }

    private fun buildWebhookMessage(event : CommandEvent) : WebhookMessage {
        val builder = WebhookMessageBuilder()
        builder.setContent(FormattingUtils.cleanSayCommand(event))
        builder.setAvatarUrl(event.selfUser.avatarUrl)
        builder.setUsername(event.selfMember.effectiveName)
        return builder.build()
    }
}
