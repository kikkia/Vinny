package com.bot.commands.general

import com.bot.commands.GeneralCommand
import com.bot.db.ChannelDAO
import com.bot.models.PixivPost
import com.bot.utils.HttpUtils
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.ChannelType

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

        // TODO: Move to static helper
        isNSFWAllowed = if (!commandEvent.isFromType(ChannelType.PRIVATE)) {
            val channel = channelDAO.getTextChannelForId(commandEvent.textChannel.id, true)

            if (channel == null) {
                commandEvent.reply(commandEvent.client.error + " Something went wrong getting the channel from the db. Please try again.")
                metricsManager.markCommandFailed(this, commandEvent.author, commandEvent.guild)
                return
            }

            channel.isNSFWEnabled
        } else {
            true
        }

        commandEvent.channel.sendTyping().queue()
        var pixivPost: PixivPost? = null

        try {
            pixivPost = if (commandEvent.args.isEmpty())
                HttpUtils.getRandomNewPixivPost(isNSFWAllowed, null)
            else
                HttpUtils.getRandomNewPixivPost(isNSFWAllowed, commandEvent.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
        } catch (e: Exception) {
            logger.severe("failed to get pixiv post", e)
            commandEvent.replyError("Something went wrong getting the post.")
            return
        }

        if (pixivPost == null) {
            commandEvent.replyWarning("No results were found!")
            return
        }

        val embedBuilder = EmbedBuilder()
        embedBuilder.setThumbnail(pixivPost.url)
        embedBuilder.setTitle(pixivPost.title)
        embedBuilder.setDescription(pixivPost.url)
        embedBuilder.setFooter("Author: " + pixivPost.authorName, null)
        commandEvent.reply(embedBuilder.build())
    }
}
