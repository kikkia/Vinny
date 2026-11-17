package com.bot.commands.slash.scheduled

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.ScheduledCommandDAO
import com.bot.exceptions.IntervalFormatException
import com.bot.exceptions.newstyle.ChannelTypeNotSupportedException
import com.bot.models.ScheduledCommand
import com.bot.models.UsageLevel
import com.bot.utils.ChanUtils
import com.bot.utils.CommandCategories
import com.bot.utils.FormattingUtils
import com.bot.utils.RssUtils
import com.bot.utils.TheGreatCCPFilter
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.sql.SQLException
import java.util.logging.Level

class ScheduleSlashCommand : BaseSlashCommand() {

    private val scheduledCommandDAO: ScheduledCommandDAO = ScheduledCommandDAO.getInstance()

    companion object {
        private val SCHEDULABLE_COMMANDS = listOf(
            Choice("rr - Random hot Reddit post (args: subreddit)", "rr"),
            Choice("nr - Random new Reddit post (args: subreddit)", "nr"),
            Choice("tr - Random top Reddit post (args: subreddit)", "tr"),
            Choice("r34 - Rule34 search (args: tags)", "r34"),
            Choice("e621 - e621 search (args: tags)", "e621"),
            Choice("4chan - Random 4chan thread (args: board)", "4chan"),
            Choice("shitpost - Random shitpost (no args)", "shitpost"),
            Choice("say - Repeat a message (args: text)", "say")
        )

        private val ALLOWED_COMMAND_NAMES = SCHEDULABLE_COMMANDS.map { it.asString }.toSet()
        private val NSFW_ONLY_COMMANDS = setOf("r34", "e621", "4chan")
        private val NO_ARGS_COMMANDS = setOf("shitpost")

        private val INTERVAL_PRESETS = listOf(
            Choice("5 minutes (5:00)", "5:00"),
            Choice("15 minutes (15:00)", "15:00"),
            Choice("30 minutes (30:00)", "30:00"),
            Choice("1 hour (1:00:00)", "1:00:00"),
            Choice("6 hours (6:00:00)", "6:00:00"),
            Choice("12 hours (12:00:00)", "12:00:00"),
            Choice("1 day (1:00:00:00)", "1:00:00:00"),
            Choice("1 week (1:00:00:00:00)", "1:00:00:00:00")
        )
    }

    init {
        this.name = "schedule"
        this.help = "Schedules a command to run automatically on a timer"
        this.category = CommandCategories.MODERATION
        this.botPermissions = arrayOf(Permission.MANAGE_WEBHOOKS)
        this.options = listOf(
            OptionData(OptionType.STRING, "command", "The command to schedule", true)
                .addChoices(SCHEDULABLE_COMMANDS),
            OptionData(OptionType.STRING, "interval", "How often to run (ww:dd:hh:mm:ss, minimum 5:00)", true, true),
            OptionData(OptionType.STRING, "args", "Arguments for the command (e.g. subreddit, tags, board, or text)", false),
            OptionData(OptionType.CHANNEL, "channel", "Channel to run the scheduled command in (defaults to current channel)", false)
                .setChannelTypes(ChannelType.TEXT)
        )
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val targetChannel = getTargetTextChannel(command)

        if (!command.member!!.hasPermission(targetChannel, Permission.MANAGE_CHANNEL)) {
            command.replyWarningTranslated("SCHEDULE_MISSING_PERMISSION", targetChannel.asMention)
            return
        }

        if (!command.guild!!.selfMember.hasPermission(targetChannel, Permission.MANAGE_WEBHOOKS, Permission.MESSAGE_SEND)) {
            command.replyWarningTranslated("SCHEDULE_BOT_MISSING_PERMISSION", targetChannel.asMention)
            return
        }

        val user = try {
            userDAO.getById(command.user.id)
        } catch (e: SQLException) {
            logger.log(Level.WARNING, "Did not find user", e)
            null
        }
        val usageLevel = user?.usageLevel() ?: UsageLevel.BASIC
        if (scheduledCommandDAO.getCountOfScheduledForAuthor(command.user.id) >= usageLevel.maxScheduled) {
            command.replyWarningTranslated("SCHEDULE_LIMIT_REACHED", usageLevel.maxScheduled)
            return
        }

        val cmdName = command.optString("command")?.trim()?.lowercase()
        if (cmdName == null || cmdName !in ALLOWED_COMMAND_NAMES) {
            command.replyWarningTranslated("SCHEDULE_INVALID_COMMAND")
            return
        }

        val rawArgs = command.optString("args")?.trim() ?: ""
        if (!validateCommandArgs(command, cmdName, rawArgs, targetChannel)) {
            return
        }

        val intervalInput = command.optString("interval")?.trim() ?: ""
        val interval = try {
            FormattingUtils.getTimeForScheduledInput(intervalInput)
        } catch (e: IntervalFormatException) {
            command.replyWarningTranslated("SCHEDULE_INVALID_INTERVAL", e.message ?: "Invalid format")
            return
        }

        val storedCommand = if (cmdName in NO_ARGS_COMMANDS || rawArgs.isEmpty()) {
            "~$cmdName"
        } else {
            "~$cmdName $rawArgs"
        }

        if (storedCommand.length > 500) {
            command.replyWarningTranslated("SCHEDULE_COMMAND_TOO_LONG")
            return
        }

        val scheduledCommand = ScheduledCommand(
            0,
            storedCommand,
            command.guild!!.id,
            targetChannel.id,
            command.user.id,
            interval,
            System.currentTimeMillis()
        )

        try {
            scheduledCommandDAO.addScheduledCommand(scheduledCommand)
            command.replySuccessTranslated(
                "SCHEDULE_SUCCESS",
                storedCommand,
                targetChannel.asMention,
                FormattingUtils.getDurationBreakdown(interval)
            )
        } catch (e: SQLException) {
            logger.severe("Failed to add scheduled command", e)
            command.replyGenericError()
        }
    }

    private fun getTargetTextChannel(command: ExtSlashCommandEvent): TextChannel {
        val selected = command.optMessageChannel("channel") ?: command.channel.asGuildMessageChannel()
        if (selected.type != ChannelType.TEXT) {
            throw ChannelTypeNotSupportedException("SCHEDULE_UNSUPPORTED_CHANNEL_TYPE")
        }
        return command.guild!!.getTextChannelById(selected.id)
            ?: throw ChannelTypeNotSupportedException("SCHEDULE_UNSUPPORTED_CHANNEL_TYPE")
    }

    private fun validateCommandArgs(
        command: ExtSlashCommandEvent,
        cmdName: String,
        rawArgs: String,
        targetChannel: TextChannel
    ): Boolean {
        if (cmdName in NSFW_ONLY_COMMANDS && !targetChannel.isNSFW) {
            command.replyWarningTranslated("NSFW_NOT_ALLOWED_EXCEPTION")
            return false
        }

        if (cmdName !in NO_ARGS_COMMANDS && rawArgs.isEmpty()) {
            command.replyWarningTranslated("SCHEDULE_ARGS_REQUIRED", cmdName)
            return false
        }

        when (cmdName) {
            "rr", "nr", "tr" -> {
                val subreddit = rawArgs.replace("--post-only", "").trim()
                if (subreddit.isEmpty() || !RssUtils.isSubredditValid(subreddit)) {
                    command.replyWarningTranslated("SUBSCRIPTION_SUBREDDIT_INVALID_NAME")
                    return false
                }
            }
            "r34", "e621" -> {
                if (TheGreatCCPFilter.containsNoNoTags(rawArgs)) {
                    command.replyWarningTranslated("DISCORD_BANNED_TERMS")
                    return false
                }
            }
            "4chan" -> {
                if (ChanUtils.getBoard(rawArgs) == null) {
                    command.replyWarningTranslated("SUBSCRIPTION_CHAN_BOARD_INVALID")
                    return false
                }
            }
        }
        return true
    }

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent?) {
        if (event!!.focusedOption.name == "interval") {
            val query = event.focusedOption.value.trim()
            val matches = if (query.isEmpty()) {
                INTERVAL_PRESETS
            } else {
                INTERVAL_PRESETS.filter {
                    it.name.contains(query, ignoreCase = true) || it.asString.contains(query, ignoreCase = true)
                }.ifEmpty { INTERVAL_PRESETS }
            }
            event.replyChoices(matches).queue()
        }
        super.onAutoComplete(event)
    }
}
