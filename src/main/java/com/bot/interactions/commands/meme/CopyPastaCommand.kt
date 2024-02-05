package com.bot.interactions.commands.meme

import com.bot.RedditConnection
import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.MemeCommand
import com.bot.utils.RedditHelper
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class CopyPastaCommand : MemeCommand() {

    private val redditConnection: RedditConnection

    init {
        this.name = "copypasta"
        this.help = "Gives a copy pasta"
        redditConnection = RedditConnection.getInstance()
    }

    @Trace(operationName = "executeCommand", resourceName = "CopyPasta")
    override fun executeCommand(commandEvent: CommandEvent) {
        try {
            commandEvent.reply(RedditHelper.getRandomCopyPasta(redditConnection))
        } catch (e: Exception) {
            commandEvent.replyError("Something went wrong, please try again.")
        }
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}
