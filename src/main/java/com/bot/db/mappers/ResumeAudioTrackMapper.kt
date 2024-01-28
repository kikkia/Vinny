package com.bot.db.mappers

import com.bot.db.models.ResumeAudioTrack
import com.bot.voice.GuildVoiceConnection
import java.util.*

class ResumeAudioTrackMapper {

    companion object {
        fun queueToTracks(conn: GuildVoiceConnection) : List<ResumeAudioTrack> {
            val tracks = LinkedList<ResumeAudioTrack>()
            val np = ResumeAudioTrack(conn.nowPlaying()!!.track.info.uri!!,
                conn.getPosition()!!,
                conn.nowPlaying()!!.requesterName,
                conn.nowPlaying()!!.requesterID)
            tracks.add(np)
            for (track in conn.getQueuedTracks()) {
                tracks.add(ResumeAudioTrack(track.track.info.uri!!,
                    0,
                    track.requesterName,
                    track.requesterID))
            }
            return tracks
        }
    }
}