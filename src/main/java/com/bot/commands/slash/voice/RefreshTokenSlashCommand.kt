package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.OauthConfigDAO
import com.bot.utils.Oauth2Utils

class RefreshTokenSlashCommand: VoiceSlashCommand() {
    private val oauthConfigDAO = OauthConfigDAO.getInstance()

    init {
        name = "refresh"
        help = "Manually refresh your voice credentials. This may help if you hit a lot of voice errors."
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val oauthConfig = oauthConfigDAO.getOauthConfig(command.user.id)
        if (oauthConfig == null) {
            command.replyWarning("VOICE_TOKEN_NOT_YET_SETUP")
            return
        }
        Oauth2Utils.refreshAccessToken(oauthConfig)
        command.replySuccess("VOICE_TOKEN_REFRESHED")
    }
}