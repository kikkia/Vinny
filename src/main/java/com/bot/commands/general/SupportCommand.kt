package com.bot.commands.general

import com.bot.commands.GeneralCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class SupportCommand : GeneralCommand() {
    init {
        this.name = "support"
        this.help = "Gives a link to the support server"
        this.aliases = arrayOf("bug", "report", "helppls", "pls")
    }

    //@trace(operationName = "executeCommand", resourceName = "Support")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply("To report bugs, suggest features, or just hang out, join the Vinny support server: https://discord.gg/XMwyzxZ")
    }
}
