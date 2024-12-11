package com.bot.commands.slash.subscriptions.sub

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.exceptions.NoSuchResourceException
import com.bot.models.RssProvider
import com.bot.utils.TwitchUtils.Companion.getTwitchIdForUsername
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.sql.SQLException
import java.util.*

class TwitchSubscriptionSlashCommand: AddSubscriptionSlashCommand() {
    
    init {
        name = "twitch"
        help = "Get notified when a streamer goes live"
        options = listOf(OptionData(OptionType.STRING, "user-or-url", "stream url or username", true),
            OptionData(OptionType.CHANNEL, "channel", "Channel to post in"))
    }
    
    override fun runCommand(command: ExtSlashCommandEvent) {
        var subject = command.optString("user-or-url")
        val id: String
        if (subject!!.lowercase(Locale.getDefault()).contains("https://www.twitch.tv/")
            || subject.lowercase(Locale.getDefault()).contains("https://twitch.tv/")
        ) {
            subject =
                subject.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[subject.split("/".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray().size - 1]
        }
        val subChannel = getEffectiveChannel(command)

        try {
            id = getTwitchIdForUsername(subject)
        } catch (e: NoSuchResourceException) {
            command.replyWarning("SUBSCRIPTION_TWITCH_NOT_FOUND")
            return
        } catch (e: Exception) {
            logger.severe("Failed to get user from twitch", e)
            command.replyGenericError()
            return
        }

        try {
            rssDAO.addSubscription(RssProvider.TWITCH, id, subChannel.id, command.user.id, false)
        } catch (e: SQLException) {
            logger.severe("Error adding twitch sub", e)
            command.replyError("Something went wrong adding the subscription, please try again.")
        }
        command.replySuccess("SUBSCRIPTION_TWITCH_SUCCESS")
    }
}