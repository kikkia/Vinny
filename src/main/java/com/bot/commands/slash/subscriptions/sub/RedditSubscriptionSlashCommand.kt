package com.bot.commands.slash.subscriptions.sub

import com.bot.RedditConnection.Companion.getInstance
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.models.RssProvider
import com.bot.utils.RssUtils.Companion.isSubredditValid
import net.dean.jraw.RedditClient
import net.dean.jraw.models.Subreddit
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.sql.SQLException


class RedditSubscriptionSlashCommand: AddSubscriptionSlashCommand() {
    private val redditClient: RedditClient = getInstance().client

    init {
        this.name = "subreddit"
        this.help = "Subscribe to a subreddit to get notified of new posts"
        this.options = listOf(OptionData(OptionType.STRING, "subreddit", "The subreddit to subscribe to.", true),
            OptionData(OptionType.CHANNEL, "channel", "Channel to post in"))
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val input = command.optString("subreddit")
        if (!isSubredditValid(input!!)) {
            command.replyWarning("SUBSCRIPTION_SUBREDDIT_INVALID_NAME")
            return
        }
        val subreddit: Subreddit? = try {
            redditClient.subreddit(input).about()
        } catch (ignored: Exception) {
            null
        }
        if (subreddit == null) {
            command.replyWarning("SUBSCRIPTION_SUBREDDIT_INVALID")
            return
        } else if (subreddit.isNsfw && !command.channel.asTextChannel().isNSFW) {
            command.replyWarning("SUBSCRIPTION_SUBREDDIT_NSFW")
            return
        }
        val subChannel = getEffectiveChannel(command)

        // TODO: Keyword handler
        try {
            rssDAO.addSubscription(
                RssProvider.REDDIT,
                input,
                subChannel.id,
                command.user.id,
                subreddit.isNsfw
            )
        } catch (e: SQLException) {
            logger.severe("Error adding reddit sub", e)
            command.replyGenericError()
        }
        command.replySuccess("SUBSCRIPTION_SUBREDDIT_SUCCESS")
    }

}