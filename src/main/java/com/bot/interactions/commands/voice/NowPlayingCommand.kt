package com.bot.interactions.commands.voice

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.VoiceCommand
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
                val currentTrackTime = voiceConnection.getPosition()?.let { FormattingUtils.msToMinSec(it) }
                val totalDuration = FormattingUtils.msToMinSec(nowPlaying.track.info.length)
                val track = nowPlaying.track
                val embedBuilder = EmbedBuilder()
                embedBuilder.setAuthor(track.info.author)
                embedBuilder.setDescription("[" + track.info.title + "](" + track.info.uri + ")")
                embedBuilder.addField("Time", "$currentTrackTime / $totalDuration", false)
                embedBuilder.addField("Stream", track.info.isStream.toString(), false)
                embedBuilder.addField("Volume", voiceConnection.getVolume().toString(), false)
                embedBuilder.addField("Repeat Mode", voiceConnection.getRepeatMode().ezName, false)

                // If youtube, get the thumbnail
                if (track.info.uri!!.contains("www.youtube.com")) {
                    val videoID = track.info.uri!!.split("=".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[1]
                    embedBuilder.setThumbnail("https://img.youtube.com/vi/$videoID/0.jpg")
                }
                embedBuilder.setColor(FormattingUtils.getColorForTrack(track.info.uri))
                commandEvent.reply(embedBuilder.build())
            }
        }
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}