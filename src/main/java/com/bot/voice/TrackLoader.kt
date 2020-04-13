package com.bot.voice

import com.bot.metrics.MetricsManager
import com.bot.models.TrackLoadContext
import com.bot.utils.FormattingUtils
import com.bot.utils.Logger
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Member

class TrackLoader(val trackProvider: TrackProvider,
                  val playerManager: AudioPlayerManager,
                  val guildAudioPlayer: GuildAudioPlayer) {

    fun loadTrack(trackLoadContext: TrackLoadContext, requester: Member) {
        playerManager.loadItem(trackLoadContext.trackQueryString,
                TrackResultHandler(this, trackLoadContext, requester))
    }
}

private class TrackResultHandler(val loader: TrackLoader, val trackLoadContext: TrackLoadContext, val requester: Member): AudioLoadResultHandler {
    val logger = Logger(TrackResultHandler::class.simpleName)

    override fun loadFailed(p0: FriendlyException?) {
        MetricsManager.getInstance().markTrackLoadFailed()
        // TODO: Handle error
    }

    override fun trackLoaded(p0: AudioTrack?) {
        MetricsManager.getInstance().markTrackLoadSucceed()

        val track = QueuedAudioTrack(p0, requester.effectiveName, requester.user.idLong)

        loader.trackProvider.add(track)

        if (!loader.guildAudioPlayer.isPaused())
            loader.guildAudioPlayer.play()

        trackLoadContext.replyEmbed(FormattingUtils.getAudioTrackEmbed(track, -1))
    }

    override fun noMatches() {
        trackLoadContext.replyWarn("No tracks were found :(")
    }

    override fun playlistLoaded(p0: AudioPlaylist?) {
        if (p0 == null) {
            logger.warning("Null audio playlist returned!")
            trackLoadContext.replyError("Playlist returned from YT was null :thonk:")
            return
        }
        var trackCount = 0
        for (a in p0.tracks) {
            loader.trackProvider.add(QueuedAudioTrack(a, requester.effectiveName, requester.idLong))
            trackCount++
        }

        if (!loader.guildAudioPlayer.isPaused())
            loader.guildAudioPlayer.play()

        trackLoadContext.replySuccess("Successfully loaded $trackCount tracks to the queue")
    }


}