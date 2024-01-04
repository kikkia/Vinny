package com.bot.voice

import com.bot.models.enums.RepeatMode
import java.util.concurrent.ConcurrentLinkedQueue

class TrackProvider {

    private val queue: ConcurrentLinkedQueue<QueuedAudioTrack> = ConcurrentLinkedQueue()
    private var nowPlaying: QueuedAudioTrack? = null
    private var repeatMode = RepeatMode.REPEAT_NONE
    private var shuffled = false

    fun addTrack(track: QueuedAudioTrack) {
        if (nowPlaying == null) {
            nowPlaying = track
        } else {
            queue.add(track)
        }
    }

    fun nextTrack() : QueuedAudioTrack? {
        if (repeatMode == RepeatMode.REPEAT_ONE) {
            return nowPlaying
        } else if (repeatMode == RepeatMode.REPEAT_ALL) {
            queue.add(nowPlaying)
        }

        nowPlaying = null
        if (queue.isEmpty()) {
            return null
        }
        nowPlaying = queue.poll()
        return nowPlaying
    }

    fun clearAll() {
        queue.clear()
        nowPlaying = null
    }

    fun getNowPlaying() : QueuedAudioTrack? {
        return nowPlaying
    }

    fun setRepeatMode(mode: RepeatMode) {
        repeatMode = mode
    }
}