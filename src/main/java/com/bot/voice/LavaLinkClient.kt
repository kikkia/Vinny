package com.bot.voice

import com.bot.metrics.MetricsManager
import com.bot.utils.Config
import com.jagrosh.jdautilities.command.CommandEvent
import dev.arbjerg.lavalink.client.*
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup
import net.dv8tion.jda.api.entities.Guild
import java.net.URI
import java.util.logging.Logger

class LavaLinkClient private constructor() {

    private val logger = Logger.getLogger(LavaLinkClient::class.java.name)

    public var client: LavalinkClient

    init {
        val config = Config.getInstance()
        val botID = config.getConfig(Config.DISCORD_BOT_ID).toLong()
        val lavaAddress = config.getConfig(Config.LAVALINK_ADDRESS)
        val lavaPassword = config.getConfig(Config.LAVALINK_PASSWORD)
        client = LavalinkClient(botID)
        client.addNode("Vinny-1", URI.create(lavaAddress), lavaPassword, RegionGroup.US)

        logger.info("LL Client booted")

        val metricsManager = MetricsManager.getInstance()
        client.on(EmittedEvent::class.java).subscribe { event ->
            if (event is TrackStartEvent) {
                logger.info("Is a track start event!")
            }
            val node: LavalinkNode = event.node
            logger.info("Node ${node.name} emitted event: $event")
        }
    }

    fun joinEventChannel(event: CommandEvent) {
        val member = event.member
        val memberVoiceState = member.voiceState

        if (memberVoiceState!!.inVoiceChannel()) {
            memberVoiceState.channel?.let { event.jda.directAudioController.connect(it) }
        }
    }

    fun cleanupPlayer(guild: Guild) {
        getLink(guild.idLong).destroyPlayer()
        guild.jda.directAudioController.disconnect(guild)
    }

    fun getLink(guildId: Long): Link {
        return client.getLink(guildId)
    }

    fun loadTrack(link: Link, toLoad: String, commandEvent: CommandEvent) {
        if (link.state == LinkState.DISCONNECTED) {
            joinEventChannel(commandEvent)
        }
        link.loadItem(toLoad).subscribe(LLLoadHandler(link, commandEvent))
    }

    companion object {
        @Volatile private var instance: LavaLinkClient? = null
        fun getInstance(): LavaLinkClient =
            instance ?: synchronized(this) {
                instance ?: LavaLinkClient().also { instance = it }
            }
    }
}