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

class LLLoadHandler(private val link: Link, private val event: CommandEvent) : AbstractAudioLoadResultHandler() {
    val logger = Logger.getLogger(this::class.java.name)
    override fun ontrackLoaded(result: TrackLoaded) {
        try {
            val track = result.track

            // Inner class at the end of this file
            link.createOrUpdatePlayer()
                .setTrack(track)
                .setVolume(35)
                .subscribe { player ->
                    val playingTrack = player.track
                    val trackTitle = playingTrack!!.info.title
                    event.textChannel.sendMessage("Now playing: $trackTitle").queue()
                }
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

        // This is a different way of updating the player! Choose your preference!
        // This method will also create a player if there is not one in the server yet
        link.updatePlayer { update: PlayerUpdateBuilder -> update.setTrack(firstTrack).setVolume(35) }
            .subscribe { ignored: LavalinkPlayer? ->
                event.textChannel.sendMessage("Now playing: " + firstTrack.info.title).queue()
            }
    }

    override fun noMatches() {
        event.textChannel.sendMessage("No matches found for your input!").queue()
    }

    override fun loadFailed(result: LoadFailed) {
        event.textChannel.sendMessage("Failed to load track! " + result.exception.message).queue()
    }
}