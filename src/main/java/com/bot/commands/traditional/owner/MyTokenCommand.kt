package com.bot.commands.traditional.owner

import com.bot.commands.traditional.OwnerCommand
import com.bot.db.OauthConfigDAO
import com.jagrosh.jdautilities.command.CommandEvent

class MyTokenCommand: OwnerCommand() {

    init {
        name = "mytoken"
    }

    override fun executeCommand(commandEvent: CommandEvent) {
        val dao = OauthConfigDAO.getInstance()
        val cred = dao.getOauthConfig(commandEvent.author.id)
        commandEvent.reply(cred!!.accessToken)
    }
}