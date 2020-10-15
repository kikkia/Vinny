package com.bot.commands.general

import com.bot.commands.GeneralCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace


class ReviewCommand : GeneralCommand() {
    init {
        this.name = "review"
        this.guildOnly = false
        this.help = "Leave a review for the bot!"
    }

    //@trace(operationName = "executeCommand", resourceName = "Review")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply("You can leave a review for Vinny here. Thanks for the feedback! https://bots.ondiscord.xyz/bots/276855867796881408/review")
    }
}