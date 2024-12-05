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
            commandEvent.reply("**WARNING: DO NOT USE YOUR MAIN ACCOUNT! Use a burner account!**\nThere is no guarantee that this " +
                    "account will not eventually be banned. It is unlikely, but not impossible. Login url and code generated successfully. \nPlease go to " +
                    "${oauthResponse.verificationUrl}\nThen enter code `${oauthResponse.userCode}` to login. \nI will let you know when I confirm the signin.")
        } else {
            commandEvent.replyError("Failed to generate login link, please try again. If it keeps failing please let me know on the support server.")
        }
    }
}