package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.exceptions.newstyle.UserVisibleException
import com.bot.voice.GuildVoiceProvider
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class VolumeSlashCommand: VoiceSlashCommand() {

    init {
        this.name = "volume"
        this.help = "Sets the volume"
        this.options = listOf(OptionData(OptionType.INTEGER, "volume", "0-200", true))
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val newVolume = command.optLong("volume")
        if (newVolume > 200 || newVolume < 0) {
            throw UserVisibleException("INVALID_NEW_VOLUME")
        }

        GuildVoiceProvider.getInstance().getGuildVoiceConnection(command.guild!!.idLong)?.setVolume(newVolume.toInt())
        command.replySuccessTranslated("VOLUME_SET", newVolume)
    }
}