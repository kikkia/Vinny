package com.bot.voice

import com.bot.metrics.MetricsManager
import dev.arbjerg.lavalink.client.loadbalancing.VoiceRegion
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor

class CustomJDAVoiceUpdateListener(private val jdaVoiceUpdateListener: JDAVoiceUpdateListener) : VoiceDispatchInterceptor{
    private val metricsManager = MetricsManager.instance
    private val guildVoiceProvider = GuildVoiceProvider.getInstance()
    override fun onVoiceServerUpdate(update: VoiceDispatchInterceptor.VoiceServerUpdate) {
        val region = VoiceRegion.fromEndpoint(update.endpoint)
        metricsManager!!.markConnectedVoiceRegion(region.name)
        guildVoiceProvider.getGuildVoiceConnection(update.guild).region = region
        jdaVoiceUpdateListener.onVoiceServerUpdate(update)
    }

    override fun onVoiceStateUpdate(update: VoiceDispatchInterceptor.VoiceStateUpdate): Boolean {
        // We return true if a connection was previously established.
        return jdaVoiceUpdateListener.onVoiceStateUpdate(update)
    }

    private fun fromEndpoint(endpoint: String) : VoiceRegion {
        return VoiceRegion.fromEndpoint(endpoint)
    }
}