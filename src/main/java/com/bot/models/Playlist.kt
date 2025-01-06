package com.bot.models

class Playlist(id: Int, ownerID: String?, name: String?, tracks: MutableList<AudioTrack>?) {
    var id: Int = 0
    var ownerID: String? = null
    var name: String? = null
    private var tracks: MutableList<AudioTrack>? = null

    init {
        if (id < 0) {
            throw IllegalArgumentException("ID must be set")
        }
        if (ownerID == null) {
            throw IllegalArgumentException("ownerID must be set")
        }
        if (name == null) {
            throw IllegalArgumentException("name must be set")
        }
        if (tracks == null) {
            throw IllegalArgumentException("tracks must be set")
        }

        this.id = id
        this.name = name
        this.ownerID = ownerID
        setTracks(tracks)
    }

    fun getTracks(): List<AudioTrack>? {
        return tracks
    }

    fun setTracks(tracks: MutableList<AudioTrack>) {
        this.tracks = tracks
    }

    fun addTrack(track: AudioTrack) {
        for (i in tracks!!.indices) {
            // if track.position is less than a given index then track belongs earlier in the order so insert it at the current index.
            if (tracks!![i].position > track.position) {
                tracks!!.add(i, track)
                return
            }
        }
        // track has biggest value, add to end
        tracks!!.add(track)
    }

    override fun toString(): String {
        return "Playlist: $name - ${tracks!!.size} tracks"
    }
}
