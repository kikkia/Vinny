package com.bot.voice.radio

import com.bot.utils.Logger
import com.bot.utils.VinnyConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ConcurrentHashMap

object LofiRadioService {
    const val RADIO_PREFIX = "lofiradio:"
    private val stationsUrl: String = VinnyConfig.instance().voiceConfig.radioProviderUrl
    private val playlistBaseUrl: String = VinnyConfig.instance().voiceConfig.radioStationUrl
    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val _radioStations = ConcurrentHashMap<String, LofiRadioStation>()
    val radioStations: Map<String, LofiRadioStation> = _radioStations
    private val logger = Logger(this::class.simpleName)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            loadInitialStations()
        }
    }

    private suspend fun fetchStations(): List<Pair<String, String>> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(stationsUrl))
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return if (response.statusCode() == 200) {
            parseStationsJson(response.body())
        } else {
            logger.warning("Error fetching stations: ${response.statusCode()}")
            emptyList()
        }
    }

    private fun parseStationsJson(jsonString: String): List<Pair<String, String>> {
        val stations = mutableListOf<Pair<String, String>>()
        try {
            val jsonObject = JSONObject(jsonString)
            val stationsArray = jsonObject.getJSONArray("stations")
            for (i in 0 until stationsArray.length()) {
                val stationObject = stationsArray.getJSONObject(i)
                stations.add(
                    Pair(stationObject.getString("name"), stationObject.getInt("id").toString())
                )
            }
        } catch (e: Exception) {
            logger.severe("Error parsing stations JSON: ${e.message}", e)
        }
        return stations
    }

    private suspend fun loadInitialStations() {
        val stations = fetchStations()
        stations.forEach { station ->
            _radioStations[station.second] = LofiRadioStation(station.first, station.second, playlistBaseUrl)
            logger.info("Loaded station: ${station.first} (ID: ${station.second})")
        }
    }

    fun randomStation(): LofiRadioStation {
        return _radioStations.values.random()
    }

    fun getStation(stationId: String): LofiRadioStation? {
        return _radioStations[stationId]
    }

    fun stopAllStations() {
        _radioStations.values.forEach { it.stop() }
    }
}