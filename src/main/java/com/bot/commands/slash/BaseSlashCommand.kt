package com.bot.commands.slash

import com.bot.metrics.MetricsManager
import com.jagrosh.jdautilities.command.SlashCommand
import com.jagrosh.jdautilities.command.SlashCommandEvent

abstract class BaseSlashCommand : SlashCommand() {

    private val metrics = MetricsManager.instance

    init {
        guildOnly = true
    }

    override fun execute(command: SlashCommandEvent?) {
        metrics!!.markCommand(this.name, this.category.name, command!!.user, command.guild,
            scheduled = false,
            slash = true
        )

        runCommand(command)
    }

    abstract fun runCommand(command: SlashCommandEvent)
}