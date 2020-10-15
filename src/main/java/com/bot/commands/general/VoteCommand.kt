package com.bot.commands.general

import com.bot.commands.GeneralCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class VoteCommand : GeneralCommand() {
    init {
        this.name = "vote"
        this.help = "Support Vinny by upvoting on bot lists."
    }

    //@trace(operationName = "executeCommand", resourceName = "Vote")
    override fun executeCommand(commandEvent: CommandEvent) {
        val message = "If you want to support Vinny, please go and vote for Vinny on these awesome sites: \n" +
                "https://discordbotlist.com/bots/276855867796881408\n" +
                "https://discord.boats/bot/vinny\n" +
                "https://bots.ondiscord.xyz/bots/276855867796881408\n" +
                "https://botsfordiscord.com/bot/276855867796881408\n" +
                "https://discordbots.org/bot/276855867796881408"
        commandEvent.reply(message)
    }
}
