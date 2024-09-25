package com.bot.voice

import com.bot.metrics.MetricsManager
import com.jagrosh.jdautilities.command.CommandEvent
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler
import dev.arbjerg.lavalink.client.player.LoadFailed
import dev.arbjerg.lavalink.client.player.PlaylistLoaded
import dev.arbjerg.lavalink.client.player.SearchResult
import dev.arbjerg.lavalink.client.player.TrackLoaded
import org.apache.log4j.Logger
import java.util.Queue

class ProviderLLLoadHandler(private val guildVoiceConnection: GuildVoiceConnection, private val event: CommandEvent,
                            private val suffix: String, private val currentProvider: VoiceProvider,
                            private val providers: Queue<VoiceProvider>) : AbstractAudioLoadResultHandler() {
    val logger: Logger = Logger.getLogger(this::class.java.name)
    override fun ontrackLoaded(result: TrackLoaded) {
        try {
            val track = result.track
            val queuedTrack = QueuedAudioTrack(track, event.author.name, event.author.idLong)
            guildVoiceConnection.queueTrack(queuedTrack)
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
        event.textChannel.sendMessage("No matches found for your input!").queue()
    }

    override fun loadFailed(result: LoadFailed) {
        MetricsManager.instance!!.markProviderFailed(currentProvider.name)
        if (providers.isEmpty()) {
            event.textChannel.sendMessage("Failed to load track! " + result.exception.message).queue()
            return
        }
        guildVoiceConnection.loadProviderTrack(suffix, event, providers)
        println("${currentProvider.name}: ${result.exception.message}")
        event.textChannel.sendMessage("Failed to load track! Retrying...").queue()
    }
}