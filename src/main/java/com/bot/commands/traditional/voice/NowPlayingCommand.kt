package com.bot.commands.traditional.voice

import com.bot.commands.traditional.VoiceCommand
import com.bot.utils.FormattingUtils
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import net.dv8tion.jda.api.EmbedBuilder

class NowPlayingCommand : VoiceCommand() {

    init {
        name = "nowplaying"
        aliases = arrayOf("np", "playing")
        help = "Displays information about the currently playing song."
    }

    @Trace(operationName = "executeCommand", resourceName = "NowPlaying")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        if (!voiceConnection.isConnected()) {
            commandEvent.reply(commandEvent.client.warning + " I am not connected to a voice channel.")
        } else {
            val nowPlaying = voiceConnection.nowPlaying()
            if (nowPlaying == null) {
                commandEvent.replyWarning("I am not currently playing any tracks.")
            } else {
                commandEvent.reply(FormattingUtils.getAudioTrackEmbed(nowPlaying, voiceConnection.getVolume(), voiceConnection.getRepeatMode(), voiceConnection.autoplay, voiceConnection.getNodeName()))
            }
        }
    }
}