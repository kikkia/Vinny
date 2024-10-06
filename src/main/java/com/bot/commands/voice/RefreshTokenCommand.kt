package com.bot.commands.voice

import com.bot.commands.GeneralCommand
import com.bot.db.OauthConfigDAO
import com.bot.utils.Oauth2Utils
import com.jagrosh.jdautilities.command.CommandEvent

class RefreshTokenCommand: GeneralCommand() {
    private val oauthConfigDAO = OauthConfigDAO.getInstance()
    init {
        name = "refresh"
        help = "Manually refresh your voice credentials. This may help if you hit a lot of voice errors."
        guildOnly = true
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        val oauthConfig = oauthConfigDAO.getOauthConfig(commandEvent!!.author.id)
        if (oauthConfig == null) {
            commandEvent.replyWarning("You have not signed in yet. Use the `~login` command to initially sign in.")
            return
        }
        oauthConfigDAO.setOauthConfig(Oauth2Utils.refreshAccessToken(oauthConfig))
        commandEvent.reactSuccess()
    }
}