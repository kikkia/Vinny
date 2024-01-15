package com.bot.voice

import com.bot.metrics.MetricsManager
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor

class CustomJDAVoiceUpdateListener(private val jdaVoiceUpdateListener: JDAVoiceUpdateListener) : VoiceDispatchInterceptor{
    private val metricsManager = MetricsManager.instance
    override fun onVoiceServerUpdate(update: VoiceDispatchInterceptor.VoiceServerUpdate) {
        metricsManager!!.markConnectedVoiceRegion(trimEndpoint(update.endpoint))
        jdaVoiceUpdateListener.onVoiceServerUpdate(update)
    }

    override fun onVoiceStateUpdate(update: VoiceDispatchInterceptor.VoiceStateUpdate): Boolean {
        // We return true if a connection was previously established.
        return jdaVoiceUpdateListener.onVoiceStateUpdate(update)
    }

    private fun trimEndpoint(endpoint: String): String {
        return endpoint.split(".")[0].filter { !it.isDigit() }
    }
}