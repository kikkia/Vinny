package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.OauthConfigDAO

class LogoutSlashCommand: VoiceSlashCommand(false) {
    private val oauthConfigDAO = OauthConfigDAO.getInstance()

    init {
        this.name = "logout"
        this.help = "Removes voice account you have logged in"
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val config = oauthConfigDAO.getOauthConfig(command.user.id)
        if (config == null) {
            command.replyWarning("I could not find any logged in account for you.")
            return
        }
        oauthConfigDAO.removeConfig(config.userId)
        val conn = provider.getGuildVoiceConnection(command.guild!!.idLong)
        if (conn?.oauthConfig?.userId == config.userId) {
            conn.oauthConfig = null
        }
        command.replySuccess("Successfully signed out")
    }
}