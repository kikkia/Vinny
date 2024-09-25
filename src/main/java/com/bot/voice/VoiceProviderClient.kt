package com.bot.voice

import com.bot.utils.VinnyConfig
import org.json.JSONArray
import java.time.Instant
import java.util.*

data class VoiceProvider(
        val name: String,
        val url: String,
        val health: VoiceProviderHealth
)

data class VoiceProviderHealth(
        val lastStatus: Int,
        val uptime: Double,
        val down: Boolean,
        val enabled: Boolean,
)

object VoiceProviderClient {

    private lateinit var cachedInstances: Map<String, VoiceProvider>
    private var lastRefresh: Instant = Instant.MIN
    private val cacheTTL = 1800L

    // Returns a randomized queue of healthy providers
    fun getProviders(): Queue<VoiceProvider> {
        if (lastRefresh.plusSeconds(cacheTTL).isAfter(Instant.now())) {
            return LinkedList(cachedInstances.values.shuffled())
        }
        val url = VinnyConfig.instance().voiceConfig.voiceProviderAPI ?: return LinkedList()
        val response = with(java.net.URL(url).openStream()) {
            bufferedReader().use { it.readText() }
        }
        val jsonArray = JSONArray(response)
        val newInstances = mutableListOf<VoiceProvider>()
        for (i in 0 until jsonArray.length()) {
            val nested = jsonArray.getJSONArray(i)
            val data = nested.getJSONObject(1).optJSONObject("monitor") ?: continue
            if (data.getDouble("uptime") > 80 && !data.getBoolean("down") && data.getBoolean("enabled")) {
                newInstances.add(
                        VoiceProvider(
                                data.getString("alias"),
                                data.getString("url"),
                                VoiceProviderHealth(
                                        data.getInt("last_status"),
                                        data.getDouble("uptime"),
                                        data.getBoolean("down"),
                                        data.getBoolean("enabled"),
                                )
                        )
                )
            }
        }
        cachedInstances = newInstances.associateBy { it.name }
        lastRefresh = Instant.now()
        // Shuffle the providers
        return LinkedList(cachedInstances.values.shuffled())
    }
}