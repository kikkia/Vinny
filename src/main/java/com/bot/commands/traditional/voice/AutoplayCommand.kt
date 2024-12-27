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
            commandEvent.replyWarning(translator.translate("AUTOPLAY_REQUIRES_DONOR", commandEvent.guild.locale.locale))
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