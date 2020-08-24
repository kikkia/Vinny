package com.bot.commands.general

import com.bot.commands.GeneralCommand
import com.bot.db.ChannelDAO
import com.bot.models.PixivPost
import com.bot.utils.HttpUtils
import com.jagrosh.jdautilities.command.CommandEvent

class PixivCommand : GeneralCommand() {

    private val channelDAO: ChannelDAO

    init {
        this.name = "pixiv"
        this.help = "Gets a post from pixiv"
        this.arguments = "<blank|search terms>"

        this.channelDAO = ChannelDAO.getInstance()
    }

    override fun executeCommand(commandEvent: CommandEvent) {
        var isNSFWAllowed = false

        commandEvent.channel.sendTyping().queue()
        val pixivPost: PixivPost?

        try {
            pixivPost = if (commandEvent.args.isEmpty())
                HttpUtils.getRandomNewPixivPost(null)
            else
                HttpUtils.getRandomNewPixivPost( commandEvent.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
        } catch (e: Exception) {
            logger.severe("failed to get pixiv post", e)
            commandEvent.replyError("Something went wrong getting the post.")
            return
        }

        if (pixivPost == null) {
            commandEvent.replyWarning("No results were found!")
            return
        }

        val urlParts = pixivPost.previewUrl.split('=')
        val url = "https://www.pixiv.net/en/artworks/" + urlParts[urlParts.size - 1]

        // Since Pixiv will check our referer url we need to fetch the image ourselves to embed it. This is because the discord
        // Client will try to embed the previewUrl and get a 403 (no referer header)
        commandEvent.textChannel.sendFile(HttpUtils.getUrlAsByteArray(pixivPost.previewUrl, url), "${pixivPost.id}.jpg").queue()
    }
}
