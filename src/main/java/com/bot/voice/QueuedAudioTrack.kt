package com.bot.voice

import com.bot.utils.FormattingUtils
import dev.arbjerg.lavalink.client.player.Track

class QueuedAudioTrack(track: Track, requesterName: String, requesterID: Long) : BaseAudioTrack(track, requesterName,
    requesterID
) {
    override fun toString(): String {
        return "[" + FormattingUtils.msToMinSec(track.info.length) + "] *" + track.info.title + "* requested by " + requesterName
    }
}