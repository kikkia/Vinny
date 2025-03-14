package com.bot.voice

import com.bot.exceptions.MaxQueueSizeException
import com.bot.metrics.MetricsManager
import com.bot.utils.FormattingUtils
import com.bot.commands.control.CommandControlEvent
import com.jagrosh.jdautilities.menu.OrderedMenu
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler
import dev.arbjerg.lavalink.client.player.LoadFailed
import dev.arbjerg.lavalink.client.player.PlaylistLoaded
import dev.arbjerg.lavalink.client.player.SearchResult
import dev.arbjerg.lavalink.client.player.TrackLoaded
import net.dv8tion.jda.api.entities.Message
import org.apache.log4j.Logger
import java.util.concurrent.TimeUnit

class SearchLLLoadHandler(private val guildVoiceConnection: GuildVoiceConnection, private val event: CommandControlEvent,
                          private val message: Message, private val builder: OrderedMenu.Builder) : AbstractAudioLoadResultHandler() {
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
       event.replyWarning("Only found playlists for this search. Playlist searching is not supported.")
    }

    override fun onSearchResultLoaded(result: SearchResult) {
        val tracks = result.tracks
        if (tracks.isEmpty()) {
            event.replyWarning("No tracks found!")
            return
        }
        builder.setCancel { }
            .setChoices(*arrayOfNulls(0))
            .setUsers(event.getAuthor())
            .setText("Results from search:")
            .setSelection { _: Message?, i: Int ->
                val track = result.tracks[i - 1]
                try {
                    val queuedAudioTrack = QueuedAudioTrack(track, event.getAuthorName(), event.getAuthorIdLong())
                    guildVoiceConnection.queueTrack(queuedAudioTrack)
                } catch (e: MaxQueueSizeException) {
                    event.replyWarning(e.message!!)
                    return@setSelection
                }
            }
            .setUsers(event.getAuthor())
            .setTimeout(2, TimeUnit.MINUTES)
        var i = 0
        while (i < 5 && i < result.tracks.size) {
            val track = result.tracks[i]
            builder.addChoices("`" + FormattingUtils.msToMinSec(track.info.length) + "` [**" + track.info.title + "**]")
            i++
        }
        builder.build().display(message)
    }

    override fun noMatches() {
        event.replyWarning("Nothing found for that serach")
    }

    override fun loadFailed(result: LoadFailed) {
        MetricsManager.instance!!.markTrackLoadFailed(result.exception.message)
        event.replyError("Failed to load search! " + result.exception.message)
    }
}