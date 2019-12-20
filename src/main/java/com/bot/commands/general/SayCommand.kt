package com.bot.commands.general

import com.bot.commands.GeneralCommand
import com.bot.utils.FormattingUtils
import com.jagrosh.jdautilities.command.CommandEvent

class SayCommand : GeneralCommand() {
    init {
        this.name = "say"
        this.help = "Repeat after you"
        this.arguments = "<Something to say>"
    }

    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply(FormattingUtils.cleanSayCommand(commandEvent))
    }
}
