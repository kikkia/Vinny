package com.bot.commands.slash

import com.bot.exceptions.UserVisibleException
import com.bot.metrics.MetricsManager
import com.jagrosh.jdautilities.command.SlashCommand
import com.jagrosh.jdautilities.command.SlashCommandEvent

abstract class BaseSlashCommand : SlashCommand() {
    protected val logger = com.bot.utils.Logger(this.javaClass.getSimpleName())
    protected val metrics = MetricsManager.instance

    init {
        guildOnly = true
    }

    override fun execute(command: SlashCommandEvent?) {
        command!!.deferReply().queue()
        metrics!!.markCommand(this.name, this.category.name, command.user, command.guild,
            scheduled = false,
            slash = true
        )
        val commandEvent = ExtSlashCommandEvent.fromCommandEvent(command)
        try {
            preExecute(commandEvent)
            runCommand(ExtSlashCommandEvent.fromCommandEvent(command))
            postExecute(commandEvent)
        } catch (e: UserVisibleException) {
            commandEvent.replyWarning(e.message!!)
        } catch (e: Exception) {
            logger.severe("Failed slash command", e)
        }
    }

    open fun preExecute(command: ExtSlashCommandEvent) {

    }

    open fun postExecute(command: ExtSlashCommandEvent) {

    }

    abstract fun runCommand(command: ExtSlashCommandEvent)
}