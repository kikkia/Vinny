package com.bot.commands.slash.reddit

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.exceptions.RedditRateLimitException
import com.bot.utils.CommandCategories
import com.bot.utils.CommandPermissions
import com.bot.utils.RedditHelper
import com.bot.utils.RssUtils.Companion.isSubredditValid
import net.dean.jraw.ApiException
import net.dean.jraw.NoSuchSubredditException
import net.dean.jraw.http.NetworkException
import net.dean.jraw.models.SubredditSort
import net.dean.jraw.models.TimePeriod
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.buttons.Button

class RedditSlashCommand : BaseSlashCommand() {

    private val sortOptions = listOf(
        Command.Choice("Hot", "HOT"),
        Command.Choice("Best", "BEST"),
        Command.Choice("Rising", "RISING"),
        Command.Choice("New", "NEW"),
        Command.Choice("Top", "TOP"),
        Command.Choice("Controversial", "CONTROVERSIAL")
    )

    private val timeOptions = listOf(
        Command.Choice("Hour", "HOUR"),
        Command.Choice("Day", "DAY"),
        Command.Choice("Week", "WEEK"),
        Command.Choice("Month", "MONTH"),
        Command.Choice("Year", "YEAR"),
        Command.Choice("All", "ALL"),
    )

    init {
        this.name = "reddit"
        this.help = "Get a post from any subreddit"
        this.category = CommandCategories.REDDIT
        this.options = listOf(
            OptionData(OptionType.STRING, "subreddit", "The subreddit to subscribe to.", true),
            OptionData(OptionType.STRING, "sort", "Sorting (hot, new, etc)", false, true),
            OptionData(OptionType.STRING, "period", "Time period to search", false, true)
        )
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val input = command.optString("subreddit")
        if (!isSubredditValid(input!!)) {
            command.replyWarning("SUBSCRIPTION_SUBREDDIT_INVALID_NAME")
            return
        }
        val isNSFWAllowed = CommandPermissions.allowNSFW(command)
        val sortOpt = command.optString("sort")
        val periodOpt = command.optString("period")

        val sort = if (sortOpt != null) SubredditSort.valueOf(sortOpt) else SubredditSort.HOT
        val period = if (periodOpt != null) TimePeriod.valueOf(periodOpt) else TimePeriod.WEEK

        try {
            val post = RedditHelper.getRandomSubmission(
                sort,
                period,
                input,
                isNSFWAllowed)

            val refreshButton = Button.primary("refresh-reddit-${input}-${sort.name}-${period.name}", Emoji.fromUnicode("\uD83D\uDD04"))

            command.replyToCommand(post, mutableListOf(refreshButton))
        } catch (e: NullPointerException) {
            // Subreddit not found
            command.replyWarning("SUBREDDIT_NOT_FOUND")
        } catch (e: NoSuchSubredditException) {
            command.replyWarning("SUBREDDIT_NOT_FOUND")
        } catch (e: ApiException) {
            if (e.code == "403") {
                command.replyWarning("SUBREDDIT_PRIVATE_ERROR")
            } else {
                command.replyGenericError()
            }
        } catch (e: NetworkException) {
            command.replyWarning("SUBREDDIT_NOT_FOUND")
        } catch (e: RedditRateLimitException) {
            command.replyError("REDDIT_RATE_LIMIT")
        } catch (e: Exception) {
            throw e
        }
    }

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent?) {
        var choices: List<Command.Choice>? = null
        if (event!!.focusedOption.name == "sort") {
            choices = sortOptions
        } else if (event.focusedOption.name == "period") {
            choices = timeOptions
        }
        if (choices != null) {
            event.replyChoices(choices).queue()
        }
        super.onAutoComplete(event)
    }
}