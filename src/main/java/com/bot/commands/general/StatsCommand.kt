package com.bot.commands.general

import com.bot.commands.GeneralCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class StatsCommand : GeneralCommand() {
    init {
        this.name = "stats"
        this.help = "Gives a link to detailed vinny statistics"
    }

    //@trace(operationName = "executeCommand", resourceName = "Stats")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply("To find detailed stats, you can find the stats dashboard here: $DASHBOARD_LINK")
    }

    companion object {
        const val DASHBOARD_LINK = "https://p.datadoghq.com/sb/f38e74b49-cb74461cc4"
    }
}
