package com.bot.commands.slash.scheduled

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.ScheduledCommandDAO
import com.bot.models.ScheduledCommand
import com.bot.utils.CommandCategories
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.sql.SQLException
import java.util.concurrent.TimeUnit

class ScheduledSlashCommand(private val waiter: EventWaiter) : BaseSlashCommand() {

    private val scheduledCommandDAO: ScheduledCommandDAO = ScheduledCommandDAO.getInstance()

    init {
        this.name = "scheduled"
        this.help = "Lists scheduled commands for you, a channel, or the server"
        this.category = CommandCategories.GENERAL
        this.options = listOf(
            OptionData(OptionType.STRING, "scope", "Which scheduled commands to show (defaults to channel)", false)
                .addChoices(
                    Choice("Channel", "c"),
                    Choice("Server (Guild)", "g"),
                    Choice("Mine", "me")
                ),
            OptionData(OptionType.CHANNEL, "channel", "Channel to list scheduled commands for (when scope is Channel)", false)
                .setChannelTypes(ChannelType.TEXT)
        )
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val selectedChannel = command.optMessageChannel("channel") ?: command.channel
        val scope = command.optString("scope") ?: "c"

        val commandList: List<ScheduledCommand> = try {
            when (scope) {
                "g" -> scheduledCommandDAO.getAllScheduledCommandsForGuild(command.guild!!.id)
                "me" -> scheduledCommandDAO.getAllScheduledCommandsForAuthor(command.user.id)
                else -> scheduledCommandDAO.getAllScheduledCommandsForChannel(selectedChannel.id)
            }
        } catch (e: SQLException) {
            logger.severe("Failed to get scheduled commands", e)
            command.replyGenericError()
            return
        }

        if (commandList.isEmpty()) {
            command.replyWarningTranslated("SCHEDULED_NONE_FOUND")
            return
        }

        val header = when (scope) {
            "g" -> "Scheduled commands in ${command.guild!!.name}"
            "me" -> "Scheduled commands for ${command.user.asMention}"
            else -> "Scheduled commands in ${selectedChannel.name}"
        }

        val embeds = commandList.map { it.toEmbed() }
        ButtonEmbedPaginator.Builder()
            .setEventWaiter(waiter)
            .wrapPageEnds(true)
            .setUsers(command.user)
            .setTimeout(1, TimeUnit.MINUTES)
            .setItems(embeds)
            .setText(header)
            .build()
            .paginate(command.hook, 1)
    }
}
