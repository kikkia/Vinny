package com.bot.voice.radio

import java.time.Instant
import java.time.OffsetDateTime

data class PlaylistItem(
    val id: String,
    val slug: String,
    val url: String,
    val artists: String,
    val title: String,
    val image: String?,
    val duration: Double,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime
) {
    fun getCurrentTime(): Long {
        return Instant.now().minusSeconds(startTime.toEpochSecond()).toEpochMilli()
    }
}
