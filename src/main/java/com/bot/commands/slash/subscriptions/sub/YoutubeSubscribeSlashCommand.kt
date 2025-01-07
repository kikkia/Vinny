package com.bot.commands.slash.subscriptions.sub

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.exceptions.InvalidInputException
import com.bot.exceptions.NoSuchResourceException
import com.bot.models.RssProvider
import com.bot.utils.HttpUtils
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.sql.SQLException

// TODO: Fix and finish translations
class YoutubeSubscribeSlashCommand: AddSubscriptionSlashCommand() {

    init {
        this.name = "youtube"
        this.help = "Subscribe to a youtube channel to get notified of new videos"
        this.options = listOf(
            OptionData(OptionType.STRING, "youtube-url", "The subreddit to subscribe to.", true),
            OptionData(OptionType.CHANNEL, "channel", "Channel to post in")
        )
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val channelUrl = command.optString("youtube-url")
        val subChannel = getEffectiveChannel(command)

        try {
            val id = HttpUtils.getYoutubeIdForChannelUrl(channelUrl)
            rssDAO.addSubscription(
                RssProvider.YOUTUBE,
                id,
                subChannel.id,
                command.user.id,
                false
            )
        } catch (e: NoSuchResourceException) {
            command.replyWarningTranslated("SUBSCRIPTION_YT_NOT_FOUND")
            return
        } catch (e: InvalidInputException) {
            command.replyWarningTranslated(
                "That link does not look right, make sure it is a link to their youtube.com" +
                        " channel page."
            )
            return
        } catch (e: SQLException) {
            logger.severe("Error adding youtube sub", e)
            command.replyGenericError()
            return
        } catch (e: Exception) {
            logger.severe("Failed to get user from Youtube", e)
            command.replyErrorTranslated(
                "Failed to get user from Youtube, please make sure it is a direct link to their" +
                        " channel. Please try again."
            )
            return
        }

        command.replySuccessTranslated("SUBSCRIPTION_YT_SUCCESS")
    }
}