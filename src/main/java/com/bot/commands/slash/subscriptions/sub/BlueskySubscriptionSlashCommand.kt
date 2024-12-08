package com.bot.commands.slash.subscriptions.sub

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.exceptions.BlueskyException
import com.bot.models.RssProvider
import com.bot.utils.ConstantStrings
import com.bot.utils.HttpUtils
import com.bot.utils.RssUtils.Companion.isBlueSkyHandleValid
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.sql.SQLException

class BlueskySubscriptionSlashCommand: AddSubscriptionSlashCommand() {

    init {
        name = "bluesky"
        help = "Get notified when a user posts"
        options = listOf(OptionData(OptionType.STRING, "username", "username to follow", true),
            OptionData(OptionType.CHANNEL, "channel", "Channel to post in"))
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val subject: String = command.optString("username")!!.replace("@".toRegex(), "")
        if (!isBlueSkyHandleValid(subject)) {
            command.replyWarning(ConstantStrings.BLUESKY_HANDLE_NOT_VALID)
            return
        } else if (!command.channel.asTextChannel().isNSFW) {
            command.replyWarning(ConstantStrings.BLUESKY_NSFW)
            return
        }
        val subChannel = getEffectiveChannel(command)

        try {
            val rssUrl = HttpUtils.getBlueSkyRSS(subject)

            rssDAO.addSubscription(
                RssProvider.BLUESKY,
                cleanUrl(rssUrl),
                subChannel.id,
                command.user.id,
                true
            )
        } catch (e: SQLException) {
            logger.severe("Error adding bluesky sub", e)
            command.replyError("Something went wrong adding the subscription, please try again.")
            return
        } catch (e: BlueskyException) {
            command.replyError(e.message!!)
            return
        }
        command.replySuccess(ConstantStrings.BLUESKY_SUB_SUCCESS)
    }

    private fun cleanUrl(url: String?): String {
        if (url == null || !url.contains("/profile/") || !url.contains("/rss")) {
            throw BlueskyException("Failed to parse bluesky RSS url. Please let me know on the support server.")
        }

        val profilePart = "/profile/"
        val rssPart = "/rss"

        val startIndex = url.indexOf(profilePart) + profilePart.length
        val endIndex = url.indexOf(rssPart)

        return url.substring(startIndex, endIndex)
    }
}