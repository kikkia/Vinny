package com.bot.voice

import com.bot.exceptions.MaxQueueSizeException
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

class LLLoadHandler(private val guildVoiceConnection: GuildVoiceConnection, private val event: CommandEvent) : AbstractAudioLoadResultHandler() {
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
        // They gave multiple args, assume one is the tracks.
        var trackNums = arrayOf<String>()
        if (event.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray().size == 2) trackNums =
            event.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val trackUrls: MutableList<String>
        if (trackNums.size == 2) {
            var to: Int
            val from: Int
            try {
                from = trackNums[0].toInt() - 1 // Account for zero index
                to = trackNums[1].toInt()
            } catch (e: NumberFormatException) {
                event.replyWarning("NumberFormatException: Invalid number given, please only user numeric characters.")
                return
            }
            if (from > to) {
                event.replyWarning("Error: Beginning index is bigger than ending index.")
                return
            } else if (from < 0) {
                event.replyWarning("Error: Beginning index is less than 1.")
                return
            } else if (to > result.tracks.size) {
                event.replyWarning("Error: Requesting tracks out of range. Only " + result.tracks.size + " tracks in playlist :x:")
                return
            } else if (to - from > 9) {
               event.replyWarning("Warning: Requesting number of tracks that is greater than 10. Trimming results to " +
                            "" + from + "-" + (from + 9) + " :exclamation:")
                to = from + 9
            }
            trackUrls = result.tracks.subList(from, to).map { it.info.uri!! }.toMutableList()
        } else {
           trackUrls = result.tracks.map { it.info.uri!! }.toMutableList()
        }
        val msg = event.textChannel.sendMessage("Loading tracks from playlist...").complete()
        try {
            guildVoiceConnection.queuePlaylist(trackUrls, event, msg)
        } catch (e: MaxQueueSizeException) {
            event.replyWarning(e.message!!)
        }
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