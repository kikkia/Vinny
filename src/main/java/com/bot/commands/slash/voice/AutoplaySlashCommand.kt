package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.exceptions.newstyle.UserVisibleException
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class AutoplaySlashCommand: VoiceSlashCommand() {

    init {
        this.name = "autoplay"
        this.help = "Enable or disable autoplaying new tracks when your voice queue finishes."
        this.options = listOf(OptionData(OptionType.BOOLEAN, "enable", "Enable autoplay", true))
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val enable = command.optBoolean("enable")

        if (!guildDAO.isGuildPremium(command.guild!!.id)) {
            throw UserVisibleException("AUTOPLAY_REQUIRES_DONOR")
        }

        val conn = provider.getGuildVoiceConnection(command.guild!!.idLong)
                ?: throw UserVisibleException("AUTOPLAY_REQUIRES_PLAYING")
        conn.autoplay = enable
        command.replySuccess("AUTOPLAY_SET", enable)
    }
}