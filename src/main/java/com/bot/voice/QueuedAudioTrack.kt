package com.bot.voice

import com.bot.utils.FormattingUtils
import dev.arbjerg.lavalink.client.player.Track

class QueuedAudioTrack(// Used to update metadata from LL
    var track: Track, val requesterName: String, val requesterID: Long
) {

    override fun toString(): String {
        return "[" + FormattingUtils.msToMinSec(track.info.length) + "] *" + track.info.title + "* requested by " + requesterName
    }

    fun isAutoplay(): Boolean {
        return requesterID == 0L
    }
}