package com.bot.commands.nsfw

import com.bot.commands.NSFWCommand
import com.bot.db.ChannelDAO
import com.bot.exceptions.PixivException
import com.bot.utils.PixivClient
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class PixivNSFWCommand : NSFWCommand() {

    private val channelDAO: ChannelDAO

    init {
        this.name = "pixivnsfw"
        this.help = "Gets a nswf post from pixiv"
        this.arguments = "<search terms>"
        this.aliases = arrayOf("npixiv", "nsfwpixiv")

        this.channelDAO = ChannelDAO.getInstance()
    }

    @Trace(operationName = "executeCommand", resourceName = "Pixiv")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.channel.sendTyping().queue()
        try {
            val post = PixivClient.instance!!.getRandomPixivPostFromSearch(commandEvent.args, true)
            commandEvent.reply(post.url)
            commandEvent.reply(post.previewUrl)
        } catch (e: PixivException ) {
            commandEvent.replyWarning("Something went wrong getting the pixiv post: " + e.message)
        }
    }
}
