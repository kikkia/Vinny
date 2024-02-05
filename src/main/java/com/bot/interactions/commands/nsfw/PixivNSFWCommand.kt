package com.bot.interactions.commands.nsfw

import com.bot.interactions.commands.NSFWCommand
import com.bot.db.ChannelDAO
import com.bot.exceptions.PixivException
import com.bot.interactions.InteractionEvent
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
        if (commandEvent.args.isEmpty()) {
            commandEvent.replyWarning("You must give me something to search")
            return
        }
        commandEvent.channel.sendTyping().queue()
        try {
            val post = PixivClient.instance!!.getRandomPixivPostFromSearch(commandEvent.args, true)
            commandEvent.reply(post.url)
            commandEvent.reply(post.previewUrl)
        } catch (e: PixivException ) {
            commandEvent.replyWarning("Something went wrong getting the pixiv post: " + e.message)
        }
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}
