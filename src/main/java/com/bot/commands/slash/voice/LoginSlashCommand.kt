package com.bot.commands.slash.voice

import com.bot.commands.control.SlashControlEvent
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.utils.Oauth2Utils
import com.jagrosh.jdautilities.command.CooldownScope

class LoginSlashCommand: VoiceSlashCommand(false) {
    init {
        this.name = "login"
        this.help = "Login with an account to enable voice."
        this.cooldown = 600
        this.cooldownScope = CooldownScope.USER
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val oauthResponse = Oauth2Utils.initOauthFlow(SlashControlEvent(command))
        metrics!!.markOauthGenerated(oauthResponse.successful)
        if (oauthResponse.successful) {
            command.replySuccess("VOICE_LOGIN", oauthResponse.verificationUrl, oauthResponse.userCode)
        } else {
            command.replyError("VOICE_LOGIN_GEN_FAILED")
        }
    }
}