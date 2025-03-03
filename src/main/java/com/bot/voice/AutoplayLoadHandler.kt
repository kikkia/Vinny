package com.bot.voice

import com.bot.metrics.MetricsManager
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler
import dev.arbjerg.lavalink.client.player.LoadFailed
import dev.arbjerg.lavalink.client.player.PlaylistLoaded
import dev.arbjerg.lavalink.client.player.SearchResult
import dev.arbjerg.lavalink.client.player.TrackLoaded
import org.apache.log4j.Logger

class AutoplayLoadHandler(private val guildVoiceConnection: GuildVoiceConnection) : AbstractAudioLoadResultHandler() {
    val logger: Logger = Logger.getLogger(this::class.java.name)
    override fun ontrackLoaded(result: TrackLoaded) {
        try {
            val track = result.track
            val queuedTrack = QueuedAudioTrack(track, "Autoplay", 0)
            guildVoiceConnection.queueTrack(queuedTrack)
            // Inner class at the end of this file
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }

    override fun onPlaylistLoaded(result: PlaylistLoaded) {
        guildVoiceConnection.queueTrack(QueuedAudioTrack(result.tracks[0], "Autoplay", 0))
    }

    override fun onSearchResultLoaded(result: SearchResult) {
        TODO("Not yet implemented")
    }

    override fun noMatches() {
        guildVoiceConnection.autoplayFail()
    }

    override fun loadFailed(result: LoadFailed) {
        MetricsManager.instance!!.markTrackLoadFailed(result.exception.message)
        guildVoiceConnection.autoplayFail()
    }
}