package com.bot.commands.general

import com.bot.commands.GeneralCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class PingCommand : GeneralCommand() {
    init {
        this.name = "ping"
        this.guildOnly = false
        this.help = "Gets the ping from Vinny to discord."
    }

    @Trace(operationName = "executeCommand", resourceName = "Ping")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.channel.sendMessage(commandEvent.jda.gatewayPing.toString() + "ms").complete()
    }
}
