package com.bot.voice.radio

import com.bot.voice.GuildVoiceConnection


import com.bot.metrics.MetricsManager
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler
import dev.arbjerg.lavalink.client.player.LoadFailed
import dev.arbjerg.lavalink.client.player.PlaylistLoaded
import dev.arbjerg.lavalink.client.player.SearchResult
import dev.arbjerg.lavalink.client.player.TrackLoaded
import org.apache.log4j.Logger

class LLLRadioHandler(private val guildVoiceConnection: GuildVoiceConnection, private val playlistItem: PlaylistItem) : AbstractAudioLoadResultHandler() {

    val logger = Logger.getLogger(this::class.java.name)
    override fun ontrackLoaded(result: TrackLoaded) {
        try {
            val track = result.track
            val toQueue = RadioQueuedAudioTrack(track, "Radio", 0, playlistItem)
            guildVoiceConnection.queueRadioTrack(toQueue)
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }

    override fun onPlaylistLoaded(result: PlaylistLoaded) {

    }

    override fun onSearchResultLoaded(result: SearchResult) {

    }

    override fun noMatches() {
        MetricsManager.instance!!.markRadioError("NOT_FOUND")
        guildVoiceConnection.setRadio(LofiRadioService.randomStation())
    }

    override fun loadFailed(result: LoadFailed) {
        MetricsManager.instance!!.markTrackLoadFailed(result.exception.message)
        guildVoiceConnection.sendMessageToChannel("Failed to load radio, sorry.")
        guildVoiceConnection.stopRadio()
    }
}