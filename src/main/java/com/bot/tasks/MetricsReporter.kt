package com.bot.tasks

import com.bot.ShardingManager
import com.bot.caching.MarkovModelCache
import com.bot.caching.R34Cache
import com.bot.caching.SubredditCache
import com.bot.db.MembershipDAO
import com.bot.metrics.MetricsManager
import com.bot.utils.Logger
import com.bot.voice.GuildVoiceProvider
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.entities.Activity

/**
 * Thread that just reports some less active stats every 5 seconds
 */
class MetricsReporter : Thread() {
    private val shardManager: ShardingManager = ShardingManager.getInstance()
    private val markovModelCache: MarkovModelCache = MarkovModelCache.getInstance()
    private val metricsManager: MetricsManager = MetricsManager.instance!!
    private val subredditCache: SubredditCache = SubredditCache.getInstance()
    private val r34Cache: R34Cache = R34Cache.getInstance()
    private val membershipDAO: MembershipDAO = MembershipDAO.getInstance()
    private val logger = Logger(this.javaClass.simpleName)
    private var userCount = 0

    override fun run() {
        try {
            updateMetrics()
        } catch (e: Exception) {
            logger.severe("Exception when updating metrics", e)
        }
    }

    private fun updateMetrics() {
        var guildCount = 0
        for (shard in shardManager.shards.values) {
            guildCount += shard.serverCount
            metricsManager.updatePing(shard.id, shard.jda.gatewayPing)
        }
        try {
            userCount = membershipDAO.activeUserCount
        } catch (e: Exception) {
            logger.warning("Failed to get user count", e)
        }
        val voiceConnections = GuildVoiceProvider.getInstance().getAll()
        var usersInVoice = 0
        var queuedTracks = 0
        for (conn in voiceConnections) {
            if (conn.isConnected()) {
                usersInVoice += conn.currentVoiceChannel!!.members.size - 1
                queuedTracks += (1 + conn.getQueuedTracks().size) // NP+queued
                metricsManager.markConnectedVoiceRegion(conn.currentVoiceChannel!!.region)
            }
        }
        metricsManager.updateUsersInVoice(usersInVoice)
        metricsManager.updateVoiceConnectionEntities(voiceConnections.size)
        metricsManager.updateQueuedTracks(queuedTracks)

        metricsManager.updateGuildCount(guildCount)
        metricsManager.updateUserCount(userCount)
        metricsManager.updateCacheSize("markov", markovModelCache.size)
        metricsManager.updateCacheSize("subreddit", subredditCache.size)
        metricsManager.updateCacheSize("r34", r34Cache.size)
        metricsManager.updateShards(shardManager.shardManager.shardsRunning, shardManager.shardManager.shardsQueued)
        metricsManager.updateLLStats()

        // We need to set this status after the sharding manager is built. This will ensure that it is set to this, not the default
        shardManager.shardManager.setActivity(Activity.playing("@Vinny help"))
    }
}