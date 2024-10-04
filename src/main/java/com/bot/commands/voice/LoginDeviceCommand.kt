package com.bot.commands.voice

import com.bot.commands.GeneralCommand
import com.bot.utils.Oauth2Utils
import com.jagrosh.jdautilities.command.CommandEvent

class LoginDeviceCommand : GeneralCommand() {
    init {
        name = "login"
        help = "Signin with an account to allow Vinny to play from popular video sources."
        aliases = arrayOf("signin")
        cooldown = 600
        cooldownScope = CooldownScope.USER
        guildOnly = true
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
//        if (!guildDAO.isGuildPremium(commandEvent!!.guild.id)) {
//            commandEvent.replyWarning("Voice login cannot be used on this server right now. Vinny is currently slowly rolling out " +
//                    "changes that allow you to sign in with Vinny to allow Voice commands. To enable logging in a Vinny donor can run the " +
//                    "`~donor` command to enable donor status on this server, then this command can be used. This command is currently " +
//                    "locked to keep the usage slow as we work out bugs and make sure it can scale to the many thousands of Vinny users. " +
//                    "Basically, it is just in BETA at the moment, and we are hoping that if it works well we can roll it out globally or in waves to all users soon." +
//                    " You can donate to Vinny using the server subscriptions in the Vinny support server. " +
//                    "Thanks for understanding." +
//                    " https://discord.gg/XMwyzxZ")
//            return
//        }

        val oauthResponse = Oauth2Utils.initOauthFlow(commandEvent!!)
        metricsManager.markOauthGenerated(oauthResponse.successful)
        if (oauthResponse.successful) {
            commandEvent.reply("**WARNING: DO NOT USE YOUR MAIN ACCOUNT! Use a burner account!**\nThere is no guarantee that this " +
                    "account will not eventually be banned. Login url and code generated successfully. \nPlease go to " +
                    "${oauthResponse.verificationUrl}\nThen enter code `${oauthResponse.userCode}` to login. \nI will let you know when I confirm the signin.")
        } else {
            commandEvent.replyError("Failed to generate login link, please try again. If it keeps failing please let me know on the support server.")
        }
    }
}