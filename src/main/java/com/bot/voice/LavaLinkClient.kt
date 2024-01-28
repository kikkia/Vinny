package com.bot.voice

import com.bot.utils.VinnyConfig
import com.jagrosh.jdautilities.command.CommandEvent
import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.TrackEndEvent
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup
import net.dv8tion.jda.api.entities.Guild
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

class LavaLinkClient private constructor() {

    private val logger = Logger.getLogger(LavaLinkClient::class.java.name)

    var client: LavalinkClient

    private var guildClients: ConcurrentHashMap<Long, GuildVoiceConnection>

    init {
        val config = VinnyConfig.instance()
        val botID = config.discordConfig.botId.toLong()
        client = LavalinkClient(botID)
        for (node in config.voiceConfig.nodes!!) {
            // TODO: Region support
            client.addNode(node.name, URI.create(node.address), node.password, RegionGroup.US)
        }
        logger.info("LL Client booted")

        client.on(TrackEndEvent::class.java).subscribe {event ->
            val gConn = GuildVoiceProvider.getInstance().getGuildVoiceConnection(event.guildId)
            if (gConn == null) {
                logger.warning("Received track end event, when guild not in provider")
                return@subscribe
            } else if (!event.endReason.mayStartNext) {
                logger.warning("Received track end event, may NOT start next: ${event.endReason.name}")
                return@subscribe
            }
            gConn.onTrackEnd(event)
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
        getLink(guild.idLong).destroyPlayer()
        guild.jda.directAudioController.disconnect(guild)
        guildClients.remove(guild.idLong)
    }

    fun getLink(guildId: Long): Link {
        return client.getLink(guildId)
    }

    companion object {
        @Volatile private var instance: LavaLinkClient? = null
        fun getInstance(): LavaLinkClient =
            instance ?: synchronized(this) {
                instance ?: LavaLinkClient().also { instance = it }
            }
    }
}