package com.bot.voice

import com.bot.models.enums.RepeatMode
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue

class TrackProvider {

    private var queue: LinkedList<BaseAudioTrack> = LinkedList()
    private var nowPlaying: BaseAudioTrack? = null
    private var repeatMode = RepeatMode.REPEAT_NONE

    fun addTrack(track: BaseAudioTrack) {
        if (nowPlaying == null) {
            nowPlaying = track
        } else {
            queue.add(track)
        }
    }

    fun nextTrack(skipping: Boolean) : BaseAudioTrack? {
        if (repeatMode == RepeatMode.REPEAT_ONE && !skipping) {
            return nowPlaying
        } else if (repeatMode == RepeatMode.REPEAT_ALL) {
            queue.add(nowPlaying!!)
        }

        nowPlaying = null
        if (queue.isEmpty()) {
            return null
        }
        nowPlaying = queue.poll()
        return nowPlaying
    }

    fun updateNowPlaying(np: BaseAudioTrack) {
        nowPlaying = np
    }

    fun clearAll() {
        queue.clear()
        nowPlaying = null
    }

    fun clearQueue() {
        queue.clear()
    }

    fun shuffleQueue() {
        queue = LinkedList(queue.shuffled())
    }

    fun getNowPlaying() : BaseAudioTrack? {
        return nowPlaying
    }

    fun getQueued() : List<BaseAudioTrack> {
        return queue.toList()
    }

    fun setTracks(tracks: List<BaseAudioTrack>) {
        queue = LinkedList(tracks)
    }

    fun setRepeatMode(mode: RepeatMode) {
        repeatMode = mode
    }

    fun getRepeateMode() : RepeatMode {
        return repeatMode
    }

    fun moveTrack(trackIndex: Int, newPos: Int) {
        if (trackIndex < 0 || newPos < 0 || trackIndex >= queue.size) {
            throw IllegalArgumentException("Invalid fromIndex: $trackIndex, $newPos")
        }

        val itemToMove = queue.removeAt(trackIndex)

        val newIndex = if (newPos > queue.size) queue.size else newPos

        queue.add(newIndex, itemToMove)
    }
}