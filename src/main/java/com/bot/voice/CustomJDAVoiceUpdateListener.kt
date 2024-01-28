package com.bot.voice

import com.bot.metrics.MetricsManager
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor

class CustomJDAVoiceUpdateListener(private val jdaVoiceUpdateListener: JDAVoiceUpdateListener) : VoiceDispatchInterceptor{
    private val metricsManager = MetricsManager.instance
    private val endpointRegex = "^([a-z\\-]+)[0-9]+.*:443\$".toRegex()
    override fun onVoiceServerUpdate(update: VoiceDispatchInterceptor.VoiceServerUpdate) {
        metricsManager!!.markConnectedVoiceRegion(trimEndpoint(update.endpoint))
        jdaVoiceUpdateListener.onVoiceServerUpdate(update)
    }

    override fun onVoiceStateUpdate(update: VoiceDispatchInterceptor.VoiceStateUpdate): Boolean {
        // We return true if a connection was previously established.
        return jdaVoiceUpdateListener.onVoiceStateUpdate(update)
    }

    private fun trimEndpoint(endpoint: String): String {
        val match = endpointRegex.find(endpoint) ?: return "UNKNOWN"
        return match.groupValues[1]
    }
}