package com.bot.voice

import com.bot.metrics.MetricsManager
import com.bot.commands.control.CommandControlEvent
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler
import dev.arbjerg.lavalink.client.player.LoadFailed
import dev.arbjerg.lavalink.client.player.PlaylistLoaded
import dev.arbjerg.lavalink.client.player.SearchResult
import dev.arbjerg.lavalink.client.player.TrackLoaded
import net.dv8tion.jda.api.entities.Message
import org.apache.log4j.Logger

class PlaylistLLLoadHandler(private val guildVoiceConnection: GuildVoiceConnection, private val event: CommandControlEvent,
                            private val loadingMessage: Message, private val tracks: List<String>,
                            private val index: Int, private val failedCount: Int) : AbstractAudioLoadResultHandler() {

    val logger = Logger.getLogger(this::class.java.name)
    override fun ontrackLoaded(result: TrackLoaded) {
        try {
            val track = result.track
            val queuedTrack = QueuedAudioTrack(track, event.getAuthorName(), event.getAuthorIdLong())
            guildVoiceConnection.queuePlaylistTrack(queuedTrack, event, loadingMessage, tracks, index, failedCount)
            // Inner class at the end of this file
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }

    override fun onPlaylistLoaded(result: PlaylistLoaded) {
       event.replyWarning("One of these tracks is a playlist, this should not be happening. " +
                "Feel free to reach out on the support server with the playlistID.")
    }

    override fun onSearchResultLoaded(result: SearchResult) {
        event.replyWarning("One of these tracks is a search, this should not be happening. " +
                "Feel free to reach out on the support server with the playlistID.")
    }

    override fun noMatches() {
        event.replyWarning("Track, ${tracks[index]} had no matches.")
    }

    override fun loadFailed(result: LoadFailed) {
        //event.replyWarning("Track, ${tracks[index]} failed to load, but continuing to load tracks.")
        MetricsManager.instance!!.markTrackLoadFailed(result.exception.message)
        guildVoiceConnection.queuePlaylistTrack(null, event, loadingMessage, tracks, index, failedCount+1)
    }
}