package com.bot.commands.voice

import com.bot.commands.GeneralCommand
import com.bot.db.OauthConfigDAO
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.CooldownScope

class LogoutDeviceCommand : GeneralCommand() {
    private val oauthConfigDAO = OauthConfigDAO.getInstance()
    init {
        name = "logout"
        help = "Logs out of your account used by Vinny to play from popular video sources."
        aliases = arrayOf("signout")
        cooldown = 10
        cooldownScope = CooldownScope.USER
        guildOnly = true
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        val config = oauthConfigDAO.getOauthConfig(commandEvent!!.author.id)
        if (config == null) {
            commandEvent.replyWarning("I could not find any logged in account for you.")
            return
        }
        oauthConfigDAO.removeConfig(config.userId)
        commandEvent.reactSuccess()
    }
}