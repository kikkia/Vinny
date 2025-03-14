package com.bot.voice

import com.bot.metrics.MetricsManager
import com.bot.utils.VinnyConfig
import com.jagrosh.jdautilities.command.CommandEvent
import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.NodeOptions
import dev.arbjerg.lavalink.client.event.*
import dev.arbjerg.lavalink.client.loadbalancing.IRegionFilter
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup
import dev.arbjerg.lavalink.client.loadbalancing.VoiceRegion
import net.dv8tion.jda.api.entities.Guild
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

class LavaLinkClient private constructor() {

    private val logger = Logger.getLogger(LavaLinkClient::class.java.name)
    private val metricsManager = MetricsManager.instance

    var client: LavalinkClient

    private var guildClients: ConcurrentHashMap<Long, GuildVoiceConnection>
    val nodeHealth = HashMap<String, LLNodeHealthMonitor>()

    init {
        val config = VinnyConfig.instance()
        val botID = config.discordConfig.botId.toLong()
        client = LavalinkClient(botID)
        client.loadBalancer = LLNodeLoadBalancer(client, nodeHealth)
        for (node in config.voiceConfig.nodes!!) {
            val nodeOptions = NodeOptions.Builder(node.name, URI.create(node.address), node.password, regionGroupFromString(node.region)).build()
            val added = client.addNode(nodeOptions)
            nodeHealth[added.name] = LLNodeHealthMonitor(added)
            logger.info("Added node ${added.name} to region ${added.regionFilter.toString()}")
        }
        logger.info("LL Client started")

        client.on(ReadyEvent::class.java).subscribe {
            logger.info("LL Client ready")
        }

        client.on(TrackStartEvent::class.java).subscribe {
            nodeHealth[it.node.name]?.recordEvent(it)
        }

        client.on(TrackEndEvent::class.java).subscribe { event ->
            try {
                val gConn = GuildVoiceProvider.getInstance().getGuildVoiceConnection(event.guildId)
                if (gConn == null) {
                    return@subscribe
                } else if (!event.endReason.mayStartNext) {
                    logger.warning("Received track end event, may NOT start next: ${event.endReason.name}")
                    metricsManager!!.markLLTrackMayNotStartNext(event, event.node.name)
                    return@subscribe
                }
                gConn.onTrackEnd(event)
            } catch (e: Exception) {
                logger.severe("BIG BAD: Exception in client.on track end ${e.message}")
                metricsManager!!.markBigBadException(e, "LLTrackEndHandler")
            }
        }
        client.on(TrackExceptionEvent::class.java).subscribe {
            try {
                nodeHealth[it.node.name]?.recordEvent(it)
                metricsManager!!.markLLTrackException(it, it.node.name)
                val gConn = GuildVoiceProvider.getInstance().getGuildVoiceConnection(it.guildId)
                gConn!!.markFailedLoad(it.track, it)
                logger.warning("TRACK EXCEPTION EVENT: ${it.exception}")
            } catch (e: Exception) {
                logger.severe("BIG BAD: Exception in client.on track exception ${e.message}")
                metricsManager!!.markBigBadException(e, "LL.TrackExceptionHandler")
            }
        }
        guildClients = ConcurrentHashMap()
    }

    fun joinEventChannel(event: CommandEvent) {
        val member = event.member
        val memberVoiceState = member.voiceState

        if (memberVoiceState!!.inAudioChannel()) {
            memberVoiceState.channel?.let { event.jda.directAudioController.connect(it) }
        }
    }

    fun cleanupPlayer(guild: Guild) {
        getLink(guild.idLong).destroy()
        guild.jda.directAudioController.disconnect(guild)
        guildClients.remove(guild.idLong)
    }

    fun getLink(guildId: Long, region: VoiceRegion? = null): Link {
        return client.getOrCreateLink(guildId, region)
    }

    private fun regionGroupFromString(name: String?): IRegionFilter {
        if ((name?.lowercase() ?: "") == "eu") {
            return RegionGroup.EUROPE
        } else if ((name?.lowercase() ?: "") == "asia") {
            return RegionGroup.ASIA
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