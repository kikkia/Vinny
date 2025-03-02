package com.bot.voice

import com.bot.exceptions.MaxQueueSizeException
import com.bot.metrics.MetricsManager
import com.bot.commands.control.CommandControlEvent
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler
import dev.arbjerg.lavalink.client.player.*
import org.apache.log4j.Logger

class LLLoadHandler(private val guildVoiceConnection: GuildVoiceConnection, private val event: CommandControlEvent) : AbstractAudioLoadResultHandler() {
    val logger: Logger = Logger.getLogger(this::class.java.name)
    override fun ontrackLoaded(result: TrackLoaded) {
        try {
            val track = result.track
            val queuedTrack = QueuedAudioTrack(track, event.getAuthorName(), event.getAuthorIdLong())
            guildVoiceConnection.queueTrack(queuedTrack)
            // Inner class at the end of this file
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }

    override fun onPlaylistLoaded(result: PlaylistLoaded) {
        // They gave multiple args, assume one is the tracks.
        var trackNums = arrayOf<String>()
        if (event.getArgs().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray().size == 2) trackNums =
            event.getArgs().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val tracks: MutableList<Track>
        if (trackNums.size == 2) {
            val to: Int
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
            }
            tracks = result.tracks.subList(from, to).toMutableList()
        } else {
           tracks = result.tracks.toMutableList()
        }
        try {
            val queuedTracks = tracks.map { QueuedAudioTrack(it, event.getAuthorName(), event.getAuthorIdLong()) }.toMutableList()
            guildVoiceConnection.queueLoadedPlaylist(queuedTracks)
        } catch (e: MaxQueueSizeException) {
            event.replyWarning(e.message!!)
        }
    }

    override fun onSearchResultLoaded(result: SearchResult) {
        val tracks = result.tracks
        if (tracks.isEmpty()) {
            event.reply("No tracks found!")
            return
        }
        val firstTrack = tracks[0]

        val queuedTrack = QueuedAudioTrack(firstTrack, event.getAuthorName(), event.getAuthorIdLong())
        guildVoiceConnection.queueTrack(queuedTrack)
    }

    override fun noMatches() {
        event.replyWarning(("No matches found for your input!"))
    }

    override fun loadFailed(result: LoadFailed) {
        MetricsManager.instance!!.markTrackLoadFailed(result.exception.message)
        event.replyError("Failed to load track! The track or playlist may be private. If this keeps happening, you can try `~refresh` or " +
                "`~login` again. Those can help." + result.exception.message)
    }
}