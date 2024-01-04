package com.bot.voice

import com.jagrosh.jdautilities.command.CommandEvent
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler
import dev.arbjerg.lavalink.client.LavalinkPlayer
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.PlayerUpdateBuilder
import dev.arbjerg.lavalink.client.protocol.LoadFailed
import dev.arbjerg.lavalink.client.protocol.PlaylistLoaded
import dev.arbjerg.lavalink.client.protocol.SearchResult
import dev.arbjerg.lavalink.client.protocol.TrackLoaded
import org.apache.log4j.Logger

class LLLoadHandler(private val guildVoiceConnection: GuildVoiceConnection, private val link: Link, private val event: CommandEvent) : AbstractAudioLoadResultHandler() {
    val logger = Logger.getLogger(this::class.java.name)
    override fun ontrackLoaded(result: TrackLoaded) {
        try {
            val track = result.track
            val queuedTrack = QueuedAudioTrack(track, event.author.name, event.author.idLong)
            guildVoiceConnection.queueTrack(queuedTrack, event)
            // Inner class at the end of this file
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }

    override fun onPlaylistLoaded(result: PlaylistLoaded) {
        val trackCount = result.tracks.size
        event.textChannel
            .sendMessage("This playlist has $trackCount tracks!")
            .queue()
    }

    override fun onSearchResultLoaded(result: SearchResult) {
        val tracks = result.tracks
        if (tracks.isEmpty()) {
            event.textChannel.sendMessage("No tracks found!").queue()
            return
        }
        val firstTrack = tracks[0]

        val queuedTrack = QueuedAudioTrack(firstTrack, event.author.name, event.author.idLong)
        guildVoiceConnection.queueTrack(queuedTrack, event)
    }

    override fun noMatches() {
        event.textChannel.sendMessage("No matches found for your input!").queue()
    }

    override fun loadFailed(result: LoadFailed) {
        event.textChannel.sendMessage("Failed to load track! " + result.exception.message).queue()
    }
}