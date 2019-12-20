package com.bot.commands.meme

import com.bot.commands.MemeCommand
import com.bot.utils.FormattingUtils.clapify
import com.bot.utils.GuildUtils.getLastMessageFromChannel
import com.jagrosh.jdautilities.command.CommandEvent

class ClapCommand : MemeCommand() {
    init {
        this.name = "clap"
        this.help = ":clap:"
        this.arguments = "<message or nothing>"
    }

    override fun executeCommand(commandEvent: CommandEvent) {
        if (commandEvent.args.isEmpty()) {
            commandEvent.reply(clapify(getLastMessageFromChannel(commandEvent.textChannel, true)!!.contentStripped))
        } else
            commandEvent.reply(clapify(commandEvent.message.contentStripped))
    }
}
