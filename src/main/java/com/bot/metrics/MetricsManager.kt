package com.bot.metrics

import com.bot.models.*
import com.bot.models.enums.R34Provider
import com.bot.utils.Logger
import com.bot.utils.VinnyConfig
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.SlashCommand
import com.timgroup.statsd.NonBlockingStatsDClientBuilder
import com.timgroup.statsd.StatsDClient
import dev.arbjerg.lavalink.client.event.TrackEndEvent
import dev.arbjerg.lavalink.client.event.TrackExceptionEvent
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception

class MetricsManager private constructor() {
    // TODO: Health checks
    private var statsd: StatsDClient? = null
    private val config: VinnyConfig = VinnyConfig.instance()
    private val logger = Logger(this::class.java.name)

    init {
        // TODO: Better way of handling metrics names
        val env = if (config.discordConfig.botId.equals("276855867796881408", ignoreCase = true)) "live" else "test"
        val builder = NonBlockingStatsDClientBuilder()
        builder.hostname = config.thirdPartyConfig?.datadogHostname ?: "localhost"  /* common case: localhost */
        builder.prefix = "vinny-redux.$env"
        builder.port = 8125
        builder.constantTags = arrayOf("vinny:$env")
        statsd = builder.build()
    }

    fun markCommand(name: String, category: String, user: User, guild: Guild?, scheduled: Boolean, slash: Boolean) {
        val userTag = "user:${user.id}"
        val commandTag = "command:$name"
        val categoryTag = "category:$category"
        val scheduledTag = "scheduled:$scheduled"
        val slashTag = "slash:$slash"

        // Support guild being null (use in PMs)
        val guildOrPM = guild?.id ?: "PM"
        val guildTag = "guild:$guildOrPM"
        statsd!!.incrementCounter("command", userTag, guildTag, commandTag, categoryTag, scheduledTag, slashTag)
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

    fun markTrackPlayed(autoplay: Boolean, source: String) {
        statsd!!.incrementCounter("voice.track.played", "autoplay:${autoplay}", "source:$source")
    }

    fun markTrackEnd(eventName: String, startNext: Boolean) {
        statsd!!.incrementCounter("voice.track.ended", "eventName:$eventName", "mayStartNext:$startNext")
    }

    fun markTrackLoadFailed(message: String?) {
        statsd!!.incrementCounter("voice.track.loaded.failed", "message:$message")
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

    fun updateDailyActiveGuilds(count: Int) {
        statsd!!.recordGaugeValue("active.daily.guilds", count.toLong())
    }

    fun updateDailyActiveUsers(count: Int) {
        statsd!!.recordGaugeValue("active.daily.users", count.toLong())
    }

    fun updateDailyActiveVoiceGuilds(count: Int) {
        statsd!!.recordGaugeValue("active.daily.gvoice", count.toLong())
    }
    fun updateWeeklyActiveGuilds(count: Int) {
        statsd!!.recordGaugeValue("active.weekly.guilds", count.toLong())
    }

    fun updateWeeklyActiveUsers(count: Int) {
        statsd!!.recordGaugeValue("active.weekly.users", count.toLong())
    }
    fun updateWeeklyActiveVoiceGuilds(count: Int) {
        statsd!!.recordGaugeValue("active.weekly.gvoice", count.toLong())
    }


    fun updateMonthlyActiveGuilds(count: Int) {
        statsd!!.recordGaugeValue("active.monthly.guilds", count.toLong())
    }

    fun updateMonthlyActiveUsers(count: Int) {
        statsd!!.recordGaugeValue("active.monthly.users", count.toLong())
    }

    fun updateMonthlyActiveVoiceGuilds(count: Int) {
        statsd!!.recordGaugeValue("active.monthly.gvoice", count.toLong())
    }

    fun updateGuildCount(count: Int) {
        statsd!!.recordGaugeValue("guild.count", count.toLong())
    }

    fun updateUserCount(count: Int) {
        statsd!!.recordGaugeValue("users.count", count.toLong())
    }

    fun markActiveVoiceConnection(nodeName: String, region: String, autoplay: Boolean, radio: Boolean) {
        statsd!!.incrementCounter("connections.voice.rate.active", "node:${nodeName}", "player_region:${region}", "autoplay:${autoplay}", "radio:${radio}")
    }

    fun markIdleVoiceConnection(nodeName: String, region: String) {
        statsd!!.incrementCounter("connections.voice.rate.idle", "node:${nodeName}", "player_region:${region}")
    }

    fun updateActiveVoiceConnectionsCount(nodeName: String, nodeRegion: String, count: Long) {
        statsd!!.recordGaugeValue("connections.voice.active", count, "node:${nodeName}", "node_region:$nodeRegion")
    }

    fun updateIdleVoiceConnectionsCount(nodeName: String, nodeRegion: String, count: Long) {
        statsd!!.recordGaugeValue("connections.voice.idle", count, "node:${nodeName}", "node_region:$nodeRegion")
    }

    fun updateUsersInVoice(count: Int) {
        statsd!!.recordGaugeValue("connections.voice.users", count.toLong())
    }

    fun updateQueuedTracks(count: Int) {
        statsd!!.recordGaugeValue("connections.voice.tracks", count.toLong())
    }

    fun updateRadioPlayers(count: Int) {
        statsd!!.recordGaugeValue("voice.radio.active", count.toLong())
    }

    fun markOauthGenerated(success: Boolean) {
        statsd!!.incrementCounter("voice.oauth.generate", "success:$success")
    }

    fun markOauthComplete(success: Boolean) {
        statsd!!.incrementCounter("voice.oauth.complete", "success:$success")
    }

    fun updateTotalOuathUsers(count: Int) {
        statsd!!.recordGaugeValue("voice.oauth.users", count.toLong())
    }

    fun updateTotalHealthyOuathUsers(count: Int) {
        statsd!!.recordGaugeValue("voice.oauth.healthy", count.toLong())
    }

    fun updateTotalUnhealthyOuathUsers(count: Int) {
        statsd!!.recordGaugeValue("voice.oauth.unhealthy", count.toLong())
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

    fun markConnectionAge(minutes: Long, nodeName: String) {
        statsd!!.recordDistributionValue("connections.voice.age", minutes, "node:${nodeName}")
    }

    fun markForcedCleanupConnection() {
        statsd!!.incrementCounter("connections.voice.cleanup")
    }

    fun markR34Request(provider: R34Provider) {
        statsd!!.incrementCounter("r34.request", "provider:${provider.name}")
    }

    fun markR34ResponseSize(provider: R34Provider, size: Long) {
        statsd!!.recordDistributionValue("r34.responsesize", size, "provider:${provider.name}")
    }

    fun markR34Response(provider: R34Provider, success: Boolean) {
        statsd!!.incrementCounter("r34.response", "provider:${provider.name}", "success:$success")
    }

    fun markLLTrackException(trackExceptionEvent: TrackExceptionEvent, nodeName: String) {
        statsd!!.incrementCounter("voice.track.exception", "message:${trackExceptionEvent.exception.message}", "node:${nodeName}", "track:${trackExceptionEvent.track.info.uri}")
    }

    fun markLLTrackMayNotStartNext(trackEndEvent: TrackEndEvent, nodeName: String) {
        statsd!!.incrementCounter("voice.track.maynotstart", "message:${trackEndEvent.endReason}", "node:${nodeName}", "track:${trackEndEvent.track.info.uri}")
    }

    fun markBigBadException(exception: Exception, location: String) {
        statsd!!.incrementCounter("bigbad.exception", "message:${exception.message}", "location:${location}")
    }

    fun markRadioError(msg: String) {
        statsd!!.incrementCounter("voice.radio.error", "message:${msg}")
    }

    fun markButtonInteraction(action: String) {
        statsd!!.incrementCounter("button.interaction", "action:$action")
    }

    fun markTranslation(id: String, language: String) {
        statsd!!.incrementCounter("translation", "id:$id", "lang:$language")
    }

    fun markLLNodeHealth(name: String, value: Int) {
        statsd!!.recordGaugeValue("voice.node.health", value.toLong(), "node:$name")
    }

    fun updateConnOauth(countWithOauth: Int, countWithoutOauth: Int) {
        statsd!!.recordGaugeValue("connections.voice.oauth.enabled", countWithOauth.toLong())
        statsd!!.recordGaugeValue("connections.voice.oauth.disabled", countWithoutOauth.toLong())
    }

    fun updateConnConnections(disConn: Int, connectedCount: Int) {
        statsd!!.recordGaugeValue("connections.voice.oauth.disconnected", disConn.toLong())
        statsd!!.recordGaugeValue("connections.voice.oauth.connected", connectedCount.toLong())
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
                            updateActiveVoiceConnectionsCount(nodeName, nodeRegion, active.toLong())
                            updateIdleVoiceConnectionsCount(nodeName, nodeRegion, (total - active).toLong())
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