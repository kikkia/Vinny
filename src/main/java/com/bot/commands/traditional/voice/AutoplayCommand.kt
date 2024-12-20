package com.bot.commands.traditional.voice

import com.bot.commands.traditional.VoiceCommand
import com.jagrosh.jdautilities.command.CommandEvent

class AutoplayCommand : VoiceCommand() {
    init {
        name = "autoplay"
        help = "Enables or disables autoplay for your server. This continues to play related tracks when your voice queue runs out"
        aliases = arrayOf("ap")
        guildOnly = true
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        if (!guildDAO.isGuildPremium(commandEvent!!.guild.id)) {
            commandEvent.replyWarning("Autoplay cannot be enabled on this server. To enable autoplay a Vinny donor can run the `~donor` command to enable donor status on this server, then autoplay can be used." +
                    " You can donate to Vinny using the server subscriptions in the Vinny support server. " +
                    "I try to keep all of Vinny's features free, but autoplay does rely on some platforms that charge for use. " +
                    "The donations help cover the cost associated with hosting Vinny and features like this. Thanks for understanding." +
                    " https://discord.gg/XMwyzxZ")
            return
        }
        val conn = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild.idLong)
        if (conn == null) {
            commandEvent.replyWarning("I am not currently playing audio, I need to be playing to enable/disable autoplay.")
            return
        }
        val og = conn.autoplay
        conn.autoplay = !conn.autoplay
        commandEvent.replySuccess("Successfully set autoplay: ${!og}")
    }
}