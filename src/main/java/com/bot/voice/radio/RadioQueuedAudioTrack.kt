package com.bot.voice.radio

import com.bot.utils.FormattingUtils
import com.bot.utils.VinnyConfig
import com.bot.voice.BaseAudioTrack
import dev.arbjerg.lavalink.client.player.Track

class RadioQueuedAudioTrack(track: Track, requesterName: String, requesterID: Long, val playlistItem: PlaylistItem) : BaseAudioTrack(track, requesterName,
    requesterID
) {
    override fun toString(): String {
        return "[" + FormattingUtils.msToMinSec(track.info.length) + "] *" + playlistItem.title + "* requested by " + requesterName
    }

    override fun getUri(): String {
        return "${VinnyConfig.instance().voiceConfig.radioTrackUrl}${playlistItem.id}"
    }

    override fun isAutoplay(): Boolean {
        return true
    }
}