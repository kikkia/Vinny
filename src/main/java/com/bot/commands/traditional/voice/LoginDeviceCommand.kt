package com.bot.commands.traditional.voice

import com.bot.commands.traditional.GeneralCommand
import com.bot.utils.Oauth2Utils
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.CooldownScope

class LoginDeviceCommand : GeneralCommand() {
    init {
        name = "login"
        help = "Sign in with an account to allow Vinny to play from popular video sources."
        aliases = arrayOf("signin")
        cooldown = 600
        cooldownScope = CooldownScope.USER
        guildOnly = true
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        val oauthResponse = Oauth2Utils.initOauthFlow(commandEvent!!)
        metricsManager.markOauthGenerated(oauthResponse.successful)
        if (oauthResponse.successful) {
            commandEvent.reply(translator.translate("VOICE_LOGIN", commandEvent.guild.locale.locale, oauthResponse.verificationUrl, oauthResponse.userCode))
        } else {
            commandEvent.replyError(translator.translate("VOICE_LOGIN_GEN_FAILED", commandEvent.guild.locale.locale))
        }
    }
}