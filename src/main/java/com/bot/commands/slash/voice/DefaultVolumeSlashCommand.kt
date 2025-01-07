package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.exceptions.newstyle.UserVisibleException
import com.bot.voice.GuildVoiceProvider
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class DefaultVolumeSlashCommand: VoiceSlashCommand(false) {

    init {
        name = "default-volume"
        help = "Sets the default volume for the server"
        options = listOf(OptionData(OptionType.INTEGER, "default-volume", "The new default volume", true))
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val newVolume = command.optLong("default-volume")
        if (newVolume > 200 || newVolume < 0) {
            throw UserVisibleException("INVALID_NEW_VOLUME")
        }
        if (!guildDAO.updateGuildVolume(command.guild!!.id, newVolume.toInt())) {
            command.replyErrorTranslated("DEFAULT_VOLUME_UPDATE_ERROR")
            return
        }
        GuildVoiceProvider.getInstance().getGuildVoiceConnection(command.guild!!.idLong)?.setVolume(newVolume.toInt())
        command.replySuccessTranslated("DEFAULT_VOLUME_UPDATE_SUCCESS", newVolume)
    }
}