package com.bot.voice

import java.util.*

class TrackProvider {

    val trackQueue = LinkedList<QueuedAudioTrack>()

    fun isEmpty() : Boolean {
        return trackQueue.size == 0
    }

    fun peek() : QueuedAudioTrack {
        return trackQueue.peek()
    }

    fun add(track: QueuedAudioTrack) {
        trackQueue.add(track)
    }

    fun nextTrack(): QueuedAudioTrack {
        return trackQueue.poll()
    }

    fun removeTrackAtIndex(index: Int) {

    }

    fun clear() {
        trackQueue.clear()
    }
}