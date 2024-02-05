package com.bot.interactions.commands.general

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.GeneralCommand
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

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}
