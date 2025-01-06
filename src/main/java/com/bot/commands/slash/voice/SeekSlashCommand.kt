package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.utils.VoiceUtils.Companion.parseSeekPos
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class SeekSlashCommand: VoiceSlashCommand() {

    init {
        this.name = "seek"
        this.help = "Seek to a part of the playing track"
        this.options = listOf(OptionData(OptionType.STRING, "position", "Where to seek to", true))
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val voiceConnection = provider.getGuildVoiceConnection(command.guild!!)
        val seekPos = parseSeekPos(command.optString("position")!!)

        if (voiceConnection.nowPlaying() == null) {
            command.replyWarning("VOICE_NO_PLAYING_TRACK")
            return
        }

        voiceConnection.seek(seekPos, voiceConnection.nowPlaying()!!)
        command.replySuccess("VOICE_SEEK_SUCCESS", command.optString("position")!!)
    }
}