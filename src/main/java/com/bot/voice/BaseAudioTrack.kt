package com.bot.voice

import com.bot.utils.FormattingUtils
import dev.arbjerg.lavalink.client.player.Track

abstract class BaseAudioTrack(
    var track: Track, val requesterName: String, val requesterID: Long
) {

    override fun toString(): String {
        return "[" + FormattingUtils.msToMinSec(track.info.length) + "] *" + track.info.title + "* requested by " + requesterName
    }

    open fun getUri(): String {
        return track.info.uri.toString()
    }

    open fun getTitle(): String {
        return track.info.title
    }

    open fun getLength(): Long {
        return track.info.length
    }

    open fun getArtworkUrl(): String? {
        return track.info.artworkUrl
    }

    open fun isAutoplay(): Boolean {
        return requesterID == 0L
    }
}