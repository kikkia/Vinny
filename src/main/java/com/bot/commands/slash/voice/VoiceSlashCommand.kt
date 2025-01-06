package com.bot.commands.slash.voice

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.exceptions.newstyle.UserVisibleException
import com.bot.utils.CommandCategories
import com.bot.voice.GuildVoiceProvider

abstract class VoiceSlashCommand(val inChannelRequired: Boolean = true): BaseSlashCommand() {

    protected val provider = GuildVoiceProvider.getInstance()

    init {
        this.category = CommandCategories.VOICE
    }

    override fun preExecute(command: ExtSlashCommandEvent) {
        if (inChannelRequired) {
            // Ensure that the user is in a voice channel before using
            if (command.member!!.voiceState == null || !command.member!!.voiceState!!.inAudioChannel()) {
                throw UserVisibleException("VOICE_NOT_IN_CHANNEL")
            }
        }
        super.preExecute(command)
    }
}