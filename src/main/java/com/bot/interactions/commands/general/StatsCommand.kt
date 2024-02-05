package com.bot.interactions.commands.general

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.GeneralCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class StatsCommand : GeneralCommand() {
    init {
        this.name = "stats"
        this.help = "Gives a link to detailed vinny statistics"
        this.guildOnly = false
    }

    @Trace(operationName = "executeCommand", resourceName = "Stats")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply("To find detailed stats, you can find the stats dashboard here: $DASHBOARD_LINK")
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }

    companion object {
        const val DASHBOARD_LINK = "https://p.datadoghq.com/sb/f38e74b49-cb74461cc4"
    }
}
