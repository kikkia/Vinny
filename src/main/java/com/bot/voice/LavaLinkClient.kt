package com.bot.voice

import com.bot.metrics.MetricsManager
import com.bot.utils.VinnyConfig
import com.jagrosh.jdautilities.command.CommandEvent
import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.NodeOptions
import dev.arbjerg.lavalink.client.event.TrackEndEvent
import dev.arbjerg.lavalink.client.event.TrackExceptionEvent
import dev.arbjerg.lavalink.client.loadbalancing.IRegionFilter
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup
import net.dv8tion.jda.api.entities.Guild
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

class LavaLinkClient private constructor() {

    private val logger = Logger.getLogger(LavaLinkClient::class.java.name)
    private val metricsManager = MetricsManager.instance

    var client: LavalinkClient

    private var guildClients: ConcurrentHashMap<Long, GuildVoiceConnection>

    init {
        val config = VinnyConfig.instance()
        val botID = config.discordConfig.botId.toLong()
        client = LavalinkClient(botID)
        for (node in config.voiceConfig.nodes!!) {
            val nodeOptions = NodeOptions.Builder(node.name, URI.create(node.address), node.password, regionGroupFromString(node.region)).build()
            val added = client.addNode(nodeOptions)
            logger.info("Added node ${added.name} to region ${added.regionFilter.toString()}")
        }
        logger.info("LL Client booted")

        client.on(TrackEndEvent::class.java).subscribe { event ->
            val gConn = GuildVoiceProvider.getInstance().getGuildVoiceConnection(event.guildId)
            if (gConn == null) {
                return@subscribe
            } else if (!event.endReason.mayStartNext) {
                logger.warning("Received track end event, may NOT start next: ${event.endReason.name}")
                return@subscribe
            }
            gConn.onTrackEnd(event)
        }
        client.on(TrackExceptionEvent::class.java).subscribe {
            metricsManager!!.markLLTrackException(it)
            logger.warning("TRACK EXCEPTION EVENT: ${it.exception}")
        }
        guildClients = ConcurrentHashMap()
    }

    fun joinEventChannel(event: CommandEvent) {
        val member = event.member
        val memberVoiceState = member.voiceState

        if (memberVoiceState!!.inVoiceChannel()) {
            memberVoiceState.channel?.let { event.jda.directAudioController.connect(it) }
        }
    }

    fun cleanupPlayer(guild: Guild) {
        getLink(guild.idLong).destroy()
        guild.jda.directAudioController.disconnect(guild)
        guildClients.remove(guild.idLong)
    }

    fun getLink(guildId: Long): Link {
        return client.getOrCreateLink(guildId)
    }

    fun regionGroupFromString(name: String?): IRegionFilter {
        if ((name?.lowercase() ?: "") == "eu") {
            return RegionGroup.EUROPE
        }
        return RegionGroup.US
    }

    companion object {
        @Volatile private var instance: LavaLinkClient? = null
        fun getInstance(): LavaLinkClient =
            instance ?: synchronized(this) {
                instance ?: LavaLinkClient().also { instance = it }
            }
    }
}