package com.bot.commands.meme

import com.bot.RedditConnection
import com.bot.commands.MemeCommand
import com.bot.utils.RedditHelper
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import org.springframework.stereotype.Component

@Component
open class CopyPastaCommand(val redditConnection: RedditConnection) : MemeCommand() {

    init {
        this.name = "copypasta"
        this.help = "Gives a copy pasta"
    }

    @Trace(operationName = "executeCommand", resourceName = "CopyPasta")
    override fun executeCommand(commandEvent: CommandEvent) {
        try {
            commandEvent.reply(RedditHelper.getRandomCopyPasta(redditConnection))
        } catch (e: Exception) {
            commandEvent.replyError("Something went wrong, please try again.")
        }
    }
}
