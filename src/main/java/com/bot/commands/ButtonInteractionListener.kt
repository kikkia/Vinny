package com.bot.commands

import com.bot.metrics.MetricsManager
import com.bot.utils.R34Utils
import com.bot.utils.RedditHelper
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.models.TimePeriod
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter


class ButtonInteractionListener: ListenerAdapter() {
    val metricsManager = MetricsManager.instance

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        // Button Ids are used to encode metadata about how it should be handled, action-first approach
        // Example: refresh-reddit-memes-hot-week
        val parsedId = event.button.id!!.split("-")
        val action = parsedId.first()
        metricsManager!!.markButtonInteraction("${parsedId[0]}-${parsedId[1]}")
        when (action) {
            "refresh" -> handleRefreshButton(event)
            "controlplayer" -> handleVoiceControl(event)
        }
        super.onButtonInteraction(event)
    }

    private fun handleVoiceControl(event: ButtonInteractionEvent) {
        TODO("Not yet implemented")
    }

    private fun handleRefreshButton(event: ButtonInteractionEvent) {
        val platform = event.button.id!!.split("-")[1]
        when (platform) {
            "reddit" -> handleRedditRefresh(event)
            "r34" -> handleR34Refresh(event)
        }
    }

    private fun handleR34Refresh(event: ButtonInteractionEvent) {
        val search = event.button.id!!.split("-")[2]
        event.editMessage(R34Utils.getPostForSearch(search)).queue()
    }

    // Refreshes the post in the message
    private fun handleRedditRefresh(event: ButtonInteractionEvent) {
        val metadata = event.button.id!!.split("-")
        val subreddit = metadata[2]
        val sort = metadata[3]
        val period = metadata[4]

        val nsfwAllowed = allowNSFW(event)

        // Bless caching
        val newPost = RedditHelper.getRandomSubmission(
            SubredditSort.valueOf(sort),
            TimePeriod.valueOf(period),
            subreddit,
            nsfwAllowed)

        event.editMessage(newPost).queue()
    }

    private fun allowNSFW(event: ButtonInteractionEvent): Boolean {
        return event.channel.type == ChannelType.TEXT && event.guildChannel.asTextChannel().isNSFW
    }
}