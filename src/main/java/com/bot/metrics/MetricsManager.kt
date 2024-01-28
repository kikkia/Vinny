package com.bot.metrics

import com.bot.models.*
import com.bot.utils.Logger
import com.bot.utils.VinnyConfig
import com.jagrosh.jdautilities.command.Command
import com.timgroup.statsd.NonBlockingStatsDClient
import com.timgroup.statsd.StatsDClient
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MetricsManager private constructor() {
    // TODO: Health checks
    private var statsd: StatsDClient? = null
    private val config: VinnyConfig = VinnyConfig.instance()
    private val logger = Logger(this::class.java.name)

    init {
        // TODO: Better way of handling metrics names
        statsd =
            if (config.discordConfig.botId.equals("276855867796881408", ignoreCase = true)) {
                NonBlockingStatsDClient(
                    "vinny-redux.live",  /* prefix to any stats; may be null or empty string */
                    config.thirdPartyConfig?.datadogHostname ?: "localhost",  /* common case: localhost */
                    8125,  /* port */
                    *arrayOf("vinny:live") /* Datadog extension: Constant tags, always applied */
                )
            } else {
                NonBlockingStatsDClient(
                    "vinny-redux.test",  /* prefix to any stats; may be null or empty string */
                    config.thirdPartyConfig?.datadogHostname ?: "localhost",  /* common case: localhost */
                    8125,  /* port */
                    *arrayOf("vinny:test") /* Datadog extension: Constant tags, always applied */
                )
            }
    }

    fun markCommand(command: Command, user: User, guild: Guild?, scheduled: Boolean) {
        val userTag = "user:" + user.id
        val commandTag = "command:" + command.name
        val categoryTag = "category:" + command.category.name
        val scheduled = "scheduled:$scheduled"

        // Support guild being null (use in PMs)
        val guildOrPM = guild?.id ?: "PM"
        val guildTag = "guild:$guildOrPM"
        statsd!!.incrementCounter("command", userTag, guildTag, commandTag, categoryTag, scheduled)
    }

    fun markGuildAliasExecuted(guild: InternalGuild) {
        val sourceTag = "source:guild"
        val sourceIdTag = "sourceid:" + guild.id
        statsd!!.incrementCounter("alias", sourceTag, sourceIdTag)
    }

    fun markChannelAliasExecuted(channel: InternalTextChannel) {
        val sourceTag = "source:channel"
        val sourceIdTag = "sourceid:" + channel.id
        statsd!!.incrementCounter("alias", sourceTag, sourceIdTag)
    }

    fun markUserAliasExecuted(user: InternalUser) {
        val sourceTag = "source:user"
        val sourceIdTag = "sourceid:" + user.id
        statsd!!.incrementCounter("alias", sourceTag, sourceIdTag)
    }

    fun markScheduledCommandRan(command: ScheduledCommand) {
        val sourceTag = "scheduled"
        val guildTag = "guild:" + command.guild
        val commandTag = "command:" + command.command
        statsd!!.incrementCounter("command.scheduled", sourceTag, guildTag)
    }

    fun markCommandFailed(command: Command, user: User, guild: Guild) {
        val userTag = "user:" + user.id
        val guildTag = "guild:" + guild.id
        val commandTag = "command:" + command.name
        val categoryTag = "category:" + command.category.name
        statsd!!.incrementCounter("command.failed", userTag, guildTag, commandTag, categoryTag)
    }

    fun markTrackLoaded() {
        statsd!!.incrementCounter("voice.track.loaded")
    }

    fun markTrackPlayed() {
        statsd!!.incrementCounter("voice.track.played")
    }

    fun markTrackEnd() {
        statsd!!.incrementCounter("voice.track.ended")
    }

    fun markTrackLoadFailed() {
        statsd!!.incrementCounter("voice.track.loaded.failed")
    }

    fun updateCacheSize(name: String, count: Int) {
        statsd!!.recordGaugeValue("cache.$name.size", count.toLong())
    }

    fun markDiscordEvent(shard: Int) {
        val shardTag = "shard:$shard"
        statsd!!.incrementCounter("discord.event", shardTag)
    }

    fun markRssEventReceived(provider: RssProvider) {
        val providerTag = "provider:" + provider.name
        statsd!!.incrementCounter("rss.received", providerTag)
    }

    fun markRssEventChannelNotFound(provider: RssProvider, channelId: String) {
        val providerTag = "provider:" + provider.name
        val cId = "channelId:$channelId"
        statsd!!.incrementCounter("rss.channelNotFound", providerTag, cId)
    }

    fun markRouletteDed() {
        statsd!!.incrementCounter("roulette.ded")
    }

    fun markRouletteLive() {
        statsd!!.incrementCounter("roulette.live")
    }

    fun markCacheHit(name: String) {
        statsd!!.incrementCounter("cache.$name.hit")
    }

    fun markCacheMiss(name: String) {
        statsd!!.incrementCounter("cache.$name.miss")
    }

    fun updateGuildCount(count: Int) {
        statsd!!.recordGaugeValue("guild.count", count.toLong())
    }

    fun updateUserCount(count: Int) {
        statsd!!.recordGaugeValue("users.count", count.toLong())
    }

    fun updateActiveVoiceConnectionsCount(nodeName: String, nodeRegion: String, count: Int) {
        statsd!!.recordGaugeValue("connections.voice.active", count.toLong(), "node:${nodeName}", "node_region:${nodeRegion}")
    }

    fun updateIdleVoiceConnectionsCount(nodeName: String, nodeRegion: String, count: Int) {
        statsd!!.recordGaugeValue("connections.voice.idle", count.toLong(), "node:${nodeName}", "node_region:${nodeRegion}")
    }

    fun updateUsersInVoice(count: Int) {
        statsd!!.recordGaugeValue("connections.voice.users", count.toLong())
    }

    fun updateQueuedTracks(count: Int) {
        statsd!!.recordGaugeValue("connections.voice.tracks", count.toLong())
    }

    fun updatePing(shard: Int, ping: Long) {
        val shardTag = "shard:$shard"
        statsd!!.recordGaugeValue("discord.ping", ping, shardTag)
    }

    fun updateVoiceConnectionEntities(size: Int) {
        statsd!!.recordGaugeValue("cache.voice.size", size.toLong())
    }

    fun updateShards(healthy: Int, unhealthy: Int) {
        statsd!!.recordGaugeValue("shards.healthy", healthy.toLong())
        statsd!!.recordGaugeValue("shards.unhealthy", unhealthy.toLong())
    }

    fun markConnectedVoiceRegion(region: String) {
        statsd!!.incrementCounter("connections.voice.regional", "disc_region:$region")
    }

    fun updateLLStats() {
        for (node in config.voiceConfig.nodes!!) {
            val llUrl = node.address.replace("ws://", "")
            val llToken = node.password
            val nodeName = node.name
            val nodeRegion = node.region ?: "N/A"
            val request = Request.Builder()
                .url("http://$llUrl/v4/stats")  // Build the URL from the request
                .addHeader("Authorization", llToken)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle request failure here
                    logger.severe("LL stats request failed with error: ${e.message}", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    // Handle successful response here
                    if (!response.isSuccessful) {
                        logger.warning("LL stats request failed with status code: ${response.code}")
                    } else {
                        try {
                            val jsonResponse = response.body?.string()?.let { JSONObject(it) }
                            // Process the parsed JSON data
                            val active = jsonResponse!!.getInt("playingPlayers")
                            val total = jsonResponse.getInt("players")
                            updateActiveVoiceConnectionsCount(nodeName, nodeRegion, active)
                            updateIdleVoiceConnectionsCount(nodeName, nodeRegion, total - active)
                        } catch (jsonException: JSONException) {
                            logger.warning("LL stats error parsing JSON response: ${jsonException.message}")
                        }
                    }
                }
            })
        }
    }

    companion object {
        var instance: MetricsManager? = null
            get() {
                if (field == null) {
                    field = MetricsManager()
                }
                return field
            }
            private set
    }
}