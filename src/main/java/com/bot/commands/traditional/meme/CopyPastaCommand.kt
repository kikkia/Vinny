package com.bot.commands.traditional.meme

import com.bot.RedditConnection
import com.bot.commands.traditional.MemeCommand
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
}
