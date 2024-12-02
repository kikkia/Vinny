package com.bot.commands.meme

import com.bot.commands.MemeCommand
import com.bot.utils.FormattingUtils.clapify
import com.bot.utils.GuildUtils.getLastMessageFromChannel
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class ClapCommand : MemeCommand() {
    init {
        this.name = "clap"
        this.help = ":clap:"
        this.arguments = "<message or nothing>"
    }

    @Trace(operationName = "executeCommand", resourceName = "Clap")
    override fun executeCommand(commandEvent: CommandEvent) {
        if (commandEvent.args.isEmpty()) {
            commandEvent.reply(clapify(getLastMessageFromChannel(commandEvent.channel, true)!!.contentStripped))
        } else
            commandEvent.reply(clapify(commandEvent.message.contentStripped))
    }
}
