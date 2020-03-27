package com.bot.commands.general

import com.bot.commands.GeneralCommand
import com.jagrosh.jdautilities.command.CommandEvent

class InviteCommand : GeneralCommand() {
    init {
        this.name = "invite"
        this.help = "Sends a link to invite the bot to your server"
        this.arguments = ""
        this.guildOnly = false
    }

    override fun executeCommand(commandEvent: CommandEvent) {

        // No need to check perms here
        val user = commandEvent.author
        val privateChannel = user.openPrivateChannel().complete()
        privateChannel.sendMessage("https://discordapp.com/oauth2/authorize?client_id=276855867796881408&scope=bot&permissions=1744305473").queue()
        commandEvent.reactSuccess()
    }
}
