package com.bot.commands.slash.subscriptions

import com.bot.RedditConnection
import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.RssDAO
import com.bot.exceptions.BlueskyException
import com.bot.exceptions.InvalidInputException
import com.bot.exceptions.NoSuchResourceException
import com.bot.exceptions.newstyle.ChannelTypeNotSupportedException
import com.bot.exceptions.newstyle.UsageLimitException
import com.bot.models.RssProvider
import com.bot.models.UsageLevel
import com.bot.utils.ChanUtils
import com.bot.utils.CommandCategories
import com.bot.utils.HttpUtils
import com.bot.utils.RssUtils
import com.bot.utils.TwitchUtils
import net.dean.jraw.RedditClient
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.sql.SQLException
import java.util.Locale

class SubscribeCommand : BaseSlashCommand() {

    private val rssDAO: RssDAO = RssDAO.getInstance()
    private val redditClient: RedditClient = RedditConnection.getInstance().client

    companion object {
        private val PLATFORMS = listOf(
            Choice("YouTube", "youtube"),
            Choice("4chan", "4chan"),
            Choice("Reddit", "reddit"),
            Choice("Twitch", "twitch"),
            Choice("Bluesky", "bluesky")
        )
    }

    init {
        this.name = "subscribe"
        this.help = "Subscribe to updates from a user or topic on a supported site"
        this.category = CommandCategories.SUBSCRIPTION
        this.guildOnly = true
        this.botPermissions = arrayOf(Permission.MANAGE_WEBHOOKS)
        this.options = listOf(
            OptionData(OptionType.STRING, "platform", "The platform to subscribe to", true)
                .addChoices(PLATFORMS),
            OptionData(OptionType.STRING, "target", "Channel URL/@handle (YouTube), board (4chan), subreddit, Twitch user, or Bluesky handle", true, true),
            OptionData(OptionType.CHANNEL, "channel", "Channel to post updates in (defaults to current channel)", false)
                .setChannelTypes(ChannelType.TEXT)
        )
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val subChannel = getTargetTextChannel(command)

        if (!command.member!!.hasPermission(subChannel, Permission.MANAGE_CHANNEL)) {
            command.replyWarningTranslated("EXCEPTION_ADD_SUB_MISSING_PERMISSION", subChannel.asMention)
            return
        }

        if (!command.guild!!.selfMember.hasPermission(subChannel, Permission.MANAGE_WEBHOOKS, Permission.MESSAGE_SEND)) {
            command.replyWarningTranslated("SUBSCRIPTION_BOT_MISSING_PERMISSION", subChannel.asMention)
            return
        }

        if (!canAddSubscription(command)) {
            throw UsageLimitException("EXCEPTION_SUBSCRIPTION_LIMIT")
        }

        val platform = command.optString("platform")?.trim()?.lowercase(Locale.getDefault())
        val target = command.optString("target")?.trim() ?: ""

        when (platform) {
            "youtube" -> subscribeYoutube(command, target, subChannel)
            "4chan" -> subscribeChan(command, target, subChannel)
            "reddit" -> subscribeReddit(command, target, subChannel)
            "twitch" -> subscribeTwitch(command, target, subChannel)
            "bluesky" -> subscribeBluesky(command, target, subChannel)
            else -> command.replyWarningTranslated("SUBSCRIPTION_INVALID_PLATFORM")
        }
    }

    private fun subscribeYoutube(command: ExtSlashCommandEvent, target: String, subChannel: TextChannel) {
        val id = try {
            HttpUtils.getYoutubeIdForChannelUrl(target)
        } catch (e: NoSuchResourceException) {
            command.replyWarningTranslated("SUBSCRIPTION_YT_NOT_FOUND")
            return
        } catch (e: InvalidInputException) {
            command.replyWarningTranslated("SUBSCRIPTION_YT_INVALID_URL")
            return
        } catch (e: Exception) {
            logger.severe("Failed to get user from Youtube", e)
            command.replyGenericError()
            return
        }

        saveSubscription(command, RssProvider.YOUTUBE, id, subChannel, false, "SUBSCRIPTION_YT_SUCCESS")
    }

    private fun subscribeChan(command: ExtSlashCommandEvent, target: String, subChannel: TextChannel) {
        val cleanedBoard = target.lowercase(Locale.getDefault()).removePrefix("/").removeSuffix("/")
        val board = ChanUtils.getBoard(cleanedBoard)
        if (board == null) {
            command.replyWarningTranslated("SUBSCRIPTION_CHAN_BOARD_INVALID")
            return
        }
        if (board.nsfw && !subChannel.isNSFW) {
            command.replyWarningTranslated("SUBSCRIPTION_CHAN_BOARD_NSFW")
            return
        }

        saveSubscription(command, RssProvider.CHAN, board.name, subChannel, board.nsfw, "SUBSCRIPTION_CHAN_SUCCESS")
    }

    private fun subscribeReddit(command: ExtSlashCommandEvent, target: String, subChannel: TextChannel) {
        val subredditName = target.removePrefix("/").removePrefix("r/").removeSuffix("/")
        if (subredditName.isEmpty() || !RssUtils.isSubredditValid(subredditName)) {
            command.replyWarningTranslated("SUBSCRIPTION_SUBREDDIT_INVALID_NAME")
            return
        }

        val subreddit = try {
            redditClient.subreddit(subredditName).about()
        } catch (ignored: Exception) {
            null
        }
        if (subreddit == null) {
            command.replyWarningTranslated("SUBSCRIPTION_SUBREDDIT_INVALID")
            return
        }
        if (subreddit.isNsfw && !subChannel.isNSFW) {
            command.replyWarningTranslated("SUBSCRIPTION_SUBREDDIT_NSFW")
            return
        }

        saveSubscription(command, RssProvider.REDDIT, subredditName, subChannel, subreddit.isNsfw, "SUBSCRIPTION_SUBREDDIT_SUCCESS")
    }

    private fun subscribeTwitch(command: ExtSlashCommandEvent, target: String, subChannel: TextChannel) {
        var subject = target
        val lower = subject.lowercase(Locale.getDefault())
        if (lower.contains("https://www.twitch.tv/") || lower.contains("https://twitch.tv/")) {
            val parts = subject.split("/").filter { it.isNotEmpty() }
            if (parts.isNotEmpty()) {
                subject = parts.last()
            }
        }

        val id = try {
            TwitchUtils.getTwitchIdForUsername(subject)
        } catch (e: NoSuchResourceException) {
            command.replyWarningTranslated("SUBSCRIPTION_TWITCH_NOT_FOUND")
            return
        } catch (e: Exception) {
            logger.severe("Failed to get user from twitch", e)
            command.replyGenericError()
            return
        }

        saveSubscription(command, RssProvider.TWITCH, id, subChannel, false, "SUBSCRIPTION_TWITCH_SUCCESS")
    }

    private fun subscribeBluesky(command: ExtSlashCommandEvent, target: String, subChannel: TextChannel) {
        val handle = target.replace("@", "")
        if (!RssUtils.isBlueSkyHandleValid(handle)) {
            command.replyWarningTranslated("SUBSCRIPTION_BLUESKY_NOT_VALID")
            return
        }
        if (!subChannel.isNSFW) {
            command.replyWarningTranslated("SUBSCRIPTION_BLUESKY_NSFW")
            return
        }

        val cleanedId = try {
            val rssUrl = HttpUtils.getBlueSkyRSS(handle)
            cleanBlueskyUrl(rssUrl)
        } catch (e: BlueskyException) {
            command.replyWarning(e.message ?: "Failed to fetch BlueSky profile.")
            return
        } catch (e: Exception) {
            logger.severe("Error adding bluesky sub", e)
            command.replyGenericError()
            return
        }

        saveSubscription(command, RssProvider.BLUESKY, cleanedId, subChannel, true, "SUBSCRIPTION_BLUESKY_SUCCESS", handle)
    }

    private fun saveSubscription(
        command: ExtSlashCommandEvent,
        provider: RssProvider,
        subject: String,
        subChannel: TextChannel,
        nsfw: Boolean,
        successKey: String,
        displayName: String? = null
    ) {
        try {
            if (displayName != null) {
                rssDAO.addSubscription(provider, subject, subChannel.id, command.user.id, nsfw, displayName)
            } else {
                rssDAO.addSubscription(provider, subject, subChannel.id, command.user.id, nsfw)
            }
            command.replySuccessTranslated(successKey)
        } catch (e: SQLException) {
            logger.severe("Error adding ${provider.name} sub", e)
            command.replyGenericError()
        }
    }

    private fun canAddSubscription(command: ExtSlashCommandEvent): Boolean {
        val user = userDAO.getById(command.user.id)
        val usage = user?.usageLevel() ?: UsageLevel.BASIC
        return rssDAO.getCountForAuthor(command.user.id) < usage.maxSub
    }

    private fun getTargetTextChannel(command: ExtSlashCommandEvent): TextChannel {
        val subChannel = command.optMessageChannel("channel") ?: command.channel.asGuildMessageChannel()
        if (subChannel.type != ChannelType.TEXT) {
            throw ChannelTypeNotSupportedException("SUBSCRIPTION_UNSUPPORTED_CHANNEL_TYPE")
        }
        return command.guild!!.getTextChannelById(subChannel.id)
            ?: throw ChannelTypeNotSupportedException("SUBSCRIPTION_UNSUPPORTED_CHANNEL_TYPE")
    }

    private fun cleanBlueskyUrl(url: String?): String {
        if (url == null || !url.contains("/profile/") || !url.contains("/rss")) {
            throw BlueskyException("Failed to parse bluesky RSS url. Please let me know on the support server.")
        }

        val profilePart = "/profile/"
        val rssPart = "/rss"

        val startIndex = url.indexOf(profilePart) + profilePart.length
        val endIndex = url.indexOf(rssPart)

        return url.substring(startIndex, endIndex)
    }

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent?) {
        if (event!!.focusedOption.name == "target") {
            val platform = event.getOption("platform")?.asString
            if (platform == "4chan") {
                val selectedChan = try {
                    event.getOption("channel")?.asChannel?.let { it as? TextChannel }
                } catch (_: Exception) {
                    null
                }
                val currentChan = if (event.channelType == ChannelType.TEXT) event.channel.asTextChannel() else null
                val allowNsfw = (selectedChan ?: currentChan)?.isNSFW == true
                val boards = if (allowNsfw) ChanUtils.SFW_BOARDS + ChanUtils.NSFW_BOARDS else ChanUtils.SFW_BOARDS
                val query = event.focusedOption.value.trim().removePrefix("/")

                val choices = boards
                    .filter { query.isEmpty() || it.contains(query, ignoreCase = true) }
                    .take(25)
                    .map { Choice("/$it/", it) }
                event.replyChoices(choices).queue()
            } else {
                event.replyChoices(emptyList()).queue()
            }
        }
        super.onAutoComplete(event)
    }
}
