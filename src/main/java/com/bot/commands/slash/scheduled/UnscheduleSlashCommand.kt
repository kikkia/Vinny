package com.bot.commands.slash.scheduled

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.ScheduledCommandDAO
import com.bot.models.ScheduledCommand
import com.bot.utils.CommandCategories
import com.bot.utils.VinnyConfig
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.sql.SQLException

class UnscheduleSlashCommand : BaseSlashCommand() {

    private val scheduledCommandDAO: ScheduledCommandDAO = ScheduledCommandDAO.getInstance()
    private val ownerId: String = VinnyConfig.instance().discordConfig.ownerId

    init {
        this.name = "unschedule"
        this.help = "Removes a scheduled command by id"
        this.category = CommandCategories.MODERATION
        this.options = listOf(
            OptionData(OptionType.INTEGER, "id", "ID of the scheduled command to remove", true, true)
        )
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val id = command.optLong("id").toInt()
        val scheduledCommand: ScheduledCommand? = try {
            scheduledCommandDAO.getScheduledCommandByID(id)
        } catch (e: IndexOutOfBoundsException) {
            null
        } catch (e: SQLException) {
            logger.severe("Failed to get scheduled command from db", e)
            command.replyGenericError()
            return
        }

        if (scheduledCommand == null) {
            command.replyWarningTranslated("UNSCHEDULE_NOT_FOUND")
            return
        }

        val isBotOwner = command.user.id == ownerId
        if (!isBotOwner && scheduledCommand.guild != command.guild!!.id) {
            command.replyWarningTranslated("UNSCHEDULE_DIFF_GUILD")
            return
        }

        val targetChannel = command.guild!!.getGuildChannelById(scheduledCommand.channel)
        val hasModPerm = if (targetChannel != null) {
            command.member!!.hasPermission(targetChannel, Permission.MANAGE_CHANNEL)
        } else {
            command.member!!.hasPermission(Permission.MANAGE_SERVER)
        }

        if (!isBotOwner && command.user.id != scheduledCommand.author && !hasModPerm) {
            command.replyWarningTranslated("UNSCHEDULE_PERMISSION")
            return
        }

        try {
            scheduledCommandDAO.removeScheduledCommand(id)
            command.replySuccessTranslated("UNSCHEDULE_SUCCESS", scheduledCommand.command, id)
        } catch (e: SQLException) {
            logger.severe("Failed to remove scheduled command", e)
            command.replyGenericError()
        }
    }

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent?) {
        if (event!!.focusedOption.name == "id" && event.guild != null && event.member != null) {
            try {
                val guildCommands = scheduledCommandDAO.getAllScheduledCommandsForGuild(event.guild!!.id)
                val query = event.focusedOption.value.trim()
                val isBotOwner = event.user.id == ownerId

                val choices = guildCommands
                    .filter { cmd ->
                        if (isBotOwner || cmd.author == event.user.id) {
                            true
                        } else {
                            val ch = event.guild!!.getGuildChannelById(cmd.channel)
                            if (ch != null) event.member!!.hasPermission(ch, Permission.MANAGE_CHANNEL)
                            else event.member!!.hasPermission(Permission.MANAGE_SERVER)
                        }
                    }
                    .filter { cmd ->
                        query.isEmpty() ||
                            cmd.id.toString().contains(query) ||
                            cmd.command.contains(query, ignoreCase = true)
                    }
                    .take(25)
                    .map { cmd ->
                        Choice("${cmd.id}: ${cmd.command}".take(100), cmd.id.toLong())
                    }

                event.replyChoices(choices).queue()
            } catch (e: SQLException) {
                logger.warning("Failed to autocomplete scheduled commands", e)
            }
        }
        super.onAutoComplete(event)
    }
}
