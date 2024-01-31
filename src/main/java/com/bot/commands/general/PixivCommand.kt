package com.bot.commands.general

import com.bot.commands.GeneralCommand
import com.bot.db.ChannelDAO
import com.bot.exceptions.PixivException
import com.bot.models.PixivPost
import com.bot.utils.PixivClient
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class PixivCommand : GeneralCommand() {

    private val channelDAO: ChannelDAO

    init {
        this.name = "pixiv"
        this.help = "Gets a post from pixiv"
        this.arguments = "<search terms>"

        this.channelDAO = ChannelDAO.getInstance()
    }

    @Trace(operationName = "executeCommand", resourceName = "Pixiv")
    override fun executeCommand(commandEvent: CommandEvent) {
        if (commandEvent.args.isEmpty()) {
            commandEvent.replyWarning("You must give me something to search")
            return
        }
        commandEvent.channel.sendTyping().queue()
        try {
            val post = PixivClient.instance!!.getRandomPixivPostFromSearch(commandEvent.args, false)
            commandEvent.reply(post.url)
            commandEvent.reply(post.previewUrl)
        } catch (e: PixivException ) {
            commandEvent.replyWarning("Something went wrong getting the pixiv post: " + e.message)
        }
    }
}
