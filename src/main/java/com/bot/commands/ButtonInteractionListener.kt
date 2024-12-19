package com.bot.commands

import com.bot.db.GuildDAO
import com.bot.db.UserDAO
import com.bot.exceptions.newstyle.UserVisibleException
import com.bot.i18n.Translator
import com.bot.metrics.MetricsManager
import com.bot.service.E621Service
import com.bot.utils.R34Utils
import com.bot.utils.RedditHelper
import com.bot.voice.GuildVoiceProvider
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.models.TimePeriod
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import kotlin.random.Random


class ButtonInteractionListener: ListenerAdapter() {
    val metricsManager = MetricsManager.instance
    val translator = Translator.getInstance()
    val guildDAO = GuildDAO.getInstance()
    val userDAO = UserDAO.getInstance()

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        // Button Ids are used to encode metadata about how it should be handled, action-first approach
        // Example: refresh-reddit-memes-hot-week
        val parsedId = event.button.id!!.split("-")
        val action = parsedId.first()
        metricsManager!!.markButtonInteraction("${parsedId[0]}-${parsedId[1]}")

        guildDAO.updateLastCommandRanTime(event.guild!!.id)
        userDAO.updateLastCommandRanTime(event.user.id)

        when (action) {
            "refresh" -> handleRefreshButton(event)
            "voicecontrol" -> handleVoiceControl(event)
        }
        super.onButtonInteraction(event)
    }

    private fun handleVoiceControl(event: ButtonInteractionEvent) {
        val action = event.button.id!!.split("-")[1]

        // Do all the checks needed to verify voice is active and controllable by user
        try {
            verifyVoice(event)
        } catch (e: UserVisibleException) {
            event.reply(translator.translate(e.outputId, event.userLocale.locale)).queue()
            return
        }

        val conn = GuildVoiceProvider.getInstance().getGuildVoiceConnection(event.guild!!.idLong)
        if (conn == null) {
            event.reply(translator.translate("BUTTON_VOICE_NO_SESSION", event.userLocale.locale)).queue()
            return
        }

        when(action) {
            "playpause" -> {conn.setPaused(!conn.getPaused())}
            "next" -> {conn.nextTrack(true)}
            "repeat" -> {conn.nextRepeatMode()}
            "stop" -> {conn.cleanupPlayer()}
            "shuffle" -> {conn.shuffleTracks()}
        }
        event.deferEdit().queue()
    }

    private fun handleRefreshButton(event: ButtonInteractionEvent) {
        val platform = event.button.id!!.split("-")[1]
        when (platform) {
            "reddit" -> handleRedditRefresh(event)
            "r34" -> handleR34Refresh(event)
            "e621" -> handleE621Refresh(event)
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

    private fun handleE621Refresh(event: ButtonInteractionEvent) {
        val search = event.button.id!!.split("-")[2]
        val posts = E621Service.getInstance().getPostsForSearch(search)
        event.editMessage(posts[Random.nextInt(0, posts.size)]).queue()
    }

    private fun allowNSFW(event: ButtonInteractionEvent): Boolean {
        return event.channel.type == ChannelType.TEXT && event.guildChannel.asTextChannel().isNSFW
    }

    private fun verifyVoice(event: ButtonInteractionEvent) {
        // Voice buttons are fun since we need to do some checks for them
        // Are we in voice?
        val weAreConnected = event.guild!!.selfMember.voiceState != null && event.guild!!.selfMember.voiceState!!.inAudioChannel()
        val theyAreConnected = event.member!!.voiceState != null && event.member!!.voiceState!!.inAudioChannel()
        if (!weAreConnected) {
            throw UserVisibleException("BUTTON_VOICE_NO_SESSION")
        }
        // Are they in the same channel?
        val ourChannel = event.guild!!.selfMember.voiceState!!.channel!!.asVoiceChannel()
        if (!theyAreConnected || ourChannel != event.member!!.voiceState!!.channel!!.asVoiceChannel()) {
            throw UserVisibleException("BUTTON_VOICE_USER_NOT_IN_CHANNEL")
        }

        // Do they have perms to use voice?
        // TODO: DJ Role
    }
}