package com.bot.commands.meme

import com.bot.commands.MemeCommand
import com.jagrosh.jdautilities.command.CommandEvent

class BanHammerCommand : MemeCommand() {

    private val hammer = "░░░░░░░░░░░░\n" +
            " ▄████▄░░░░░░░░░░░░░░░░░░░░\n" +
            "██████▄░░░░░░▄▄▄░░░░░░░░░░\n" +
            "░███▀▀▀▄▄▄▀▀▀░░░░░░░░░░░░░\n" +
            "░░░▄▀▀▀▄░░░█▀▀▄░▄▀▀▄░█▄░█░\n" +
            "░░░▄▄████░░█▀▀▄░█▄▄█░█▀▄█░\n" +
            "░░░░██████░█▄▄▀░█░░█░█░▀█░\n" +
            "░░░░░▀▀▀▀░░░░░░░░░░░░░░░░░"

    init {
        this.name = "hammer"
        this.help = "bring out the hammer"
    }

    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.reply(hammer)
    }
}
