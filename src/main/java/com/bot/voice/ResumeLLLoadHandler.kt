package com.bot.voice

import com.bot.db.models.ResumeAudioGuild
import com.bot.metrics.MetricsManager
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler
import dev.arbjerg.lavalink.client.player.LoadFailed
import dev.arbjerg.lavalink.client.player.PlaylistLoaded
import dev.arbjerg.lavalink.client.player.SearchResult
import dev.arbjerg.lavalink.client.player.TrackLoaded
import net.dv8tion.jda.api.entities.Message
import org.apache.log4j.Logger

class ResumeLLLoadHandler(private val guildVoiceConnection: GuildVoiceConnection, private val loadingMessage: Message,
                          private val resumeSetup: ResumeAudioGuild, private val index: Int,
                          private val failedCount: Int) : AbstractAudioLoadResultHandler() {

    val logger = Logger.getLogger(this::class.java.name)
    override fun ontrackLoaded(result: TrackLoaded) {
        try {
            val track = result.track
            val queuedTrack = QueuedAudioTrack(track, resumeSetup.tracks[index].requesterName, resumeSetup.tracks[index].requesterId)
            guildVoiceConnection.queueResumeTrack(queuedTrack, loadingMessage, resumeSetup, index, failedCount)
            // Inner class at the end of this file
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }

    override fun onPlaylistLoaded(result: PlaylistLoaded) {

    }

    override fun onSearchResultLoaded(result: SearchResult) {

    }

    override fun noMatches() {

    }

    override fun loadFailed(result: LoadFailed) {
        //event.replyWarning("Track, ${tracks[index]} failed to load, but continuing to load tracks.")
        MetricsManager.instance!!.markTrackLoadFailed(result.exception.message)
        guildVoiceConnection.queueResumeTrack(null, loadingMessage, resumeSetup, index, failedCount+1)
    }
}