package com.bot.commands.traditional.general

import com.bot.commands.traditional.GeneralCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class InviteCommand : GeneralCommand() {
    init {
        this.name = "invite"
        this.help = "Sends a link to invite the bot to your server"
        this.arguments = ""
        this.guildOnly = false
    }

    @Trace(operationName = "executeCommand", resourceName = "Invite")
    override fun executeCommand(commandEvent: CommandEvent) {

        // No need to check perms here
        val user = commandEvent.author
        val privateChannel = user.openPrivateChannel().complete()
        privateChannel.sendMessage("https://discord.com/oauth2/authorize?client_id=276855867796881408&scope=bot+applications.commands&permissions=277796481399").queue()
        commandEvent.reactSuccess()
    }
}
