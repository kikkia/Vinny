package com.bot.voice.radio

import kotlinx.coroutines.*
import org.json.JSONArray
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.LinkedList
import kotlin.time.Duration.Companion.milliseconds

class LofiRadioStation(
    val name: String,
    val id: String,
    private val playlistUrl: String,
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
    private val refreshBuffer: Int = 1
) {
    var playlist = LinkedList<PlaylistItem>()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var nextTrackJob: Job? = null
    init {
        CoroutineScope(Dispatchers.IO).launch {
            fetchAndUpdatePlaylist()
        }
    }

    fun getNowPlaying(): PlaylistItem {
        if (playlist.isEmpty()) {
            println("EMPTY PLAYLIST FETCHING NEW")
            fetchAndUpdatePlaylist()
        }
        return playlist.first
    }

    private fun fetchPlaylist(): LinkedList<PlaylistItem> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(playlistUrl.replace("{id}", id)))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return if (response.statusCode() == 200) {
            parsePlaylistJson(response.body())
        } else {
            println("Error fetching playlist for station ${name}: ${response.statusCode()}")
            LinkedList()
        }
    }

    private fun fetchAndUpdatePlaylist() {
        playlist = fetchPlaylist()
        if (playlist.isNotEmpty()) {
            scheduleNextTrack()
        }
    }

    private fun parsePlaylistJson(jsonString: String): LinkedList<PlaylistItem> {
        val playlistItems = LinkedList<PlaylistItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val slug = "${jsonObject.getInt("id")}-${jsonObject.getInt("fileId")}"
                playlistItems.addLast(
                    PlaylistItem(
                        id = jsonObject.getInt("id").toString(),
                        slug = slug,
                        // TODO
                        url = "http://192.168.0.171:8000/music/$slug.mp3",
                        artists = jsonObject.getString("artists"),
                        title = jsonObject.getString("title"),
                        image = jsonObject.optString("image", null),
                        duration = jsonObject.getDouble("duration"),
                        startTime = OffsetDateTime.parse(jsonObject.getString("startTime")),
                        endTime = OffsetDateTime.parse(jsonObject.getString("endTime"))
                    )
                )
            }
        } catch (e: Exception) {
            println("Error parsing playlist JSON for station ${name}: ${e.message}")
        }
        return playlistItems
    }

    private fun scheduleNextTrack() {
        nextTrackJob?.cancel()

        if (playlist.isNotEmpty()) {
            val currentTrackEndTime = playlist.first.endTime

            val delay = ChronoUnit.MILLIS.between(OffsetDateTime.now(currentTrackEndTime.offset), currentTrackEndTime)

            if (delay > 0) {
                nextTrackJob = scope.launch {
                    delay(delay.milliseconds)
                    playlist.removeFirst()
                    if (playlist.size <= refreshBuffer) {
                        //println("Only ${playlist.size} track(s) left, refetching playlist for $name")
                        fetchAndUpdatePlaylist()
                    } else if (playlist.isNotEmpty()) {
                        scheduleNextTrack()
                        //println("Scheduled next track for $name, ${playlist.first.title}")
                    } else {
                        //println("Playlist for $name is empty after removing track.")
                        fetchAndUpdatePlaylist()
                    }
                }
                //println("Scheduled first track for $name, ${playlist.first.title} to end at $currentTrackEndTime (delay: ${delay}ms)")
            } else {
                playlist.removeFirst()
                if (playlist.isNotEmpty()) {
                    scheduleNextTrack()
                    //println("Scheduled next track immediately for $name, ${playlist.first.title}")
                } else {
                    //println("Playlist for $name was empty, fetched again.")
                    fetchAndUpdatePlaylist()
                }
            }
        } else {
            //println("Playlist for $name is empty, cannot schedule next track.")
            fetchAndUpdatePlaylist()
        }
    }

    fun stop() {
        nextTrackJob?.cancel()
    }
}
