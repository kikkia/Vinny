package com.bot.metrics

import com.bot.ShardingManager
import com.bot.caching.MarkovModelCache
import com.bot.caching.R34Cache
import com.bot.caching.SubredditCache
import com.bot.db.GuildDAO
import com.bot.db.MembershipDAO
import com.bot.db.OauthConfigDAO
import com.bot.db.UserDAO
import com.bot.utils.Logger
import com.bot.voice.GuildVoiceProvider
import com.bot.voice.LavaLinkClient
import net.dv8tion.jda.api.entities.Activity

/**
 * Thread that just reports some less active stats every 5 seconds
 */
class MetricsReporter : Runnable {
    private val shardManager: ShardingManager = ShardingManager.getInstance()
    private val markovModelCache: MarkovModelCache = MarkovModelCache.getInstance()
    private val metricsManager: MetricsManager = MetricsManager.instance!!
    private val subredditCache: SubredditCache = SubredditCache.getInstance()
    private val r34Cache: R34Cache = R34Cache.getInstance()
    private val membershipDAO: MembershipDAO = MembershipDAO.getInstance()
    private val guildDAO = GuildDAO.getInstance()
    private val userDAO = UserDAO.getInstance()
    private val oauthConfigDAO = OauthConfigDAO.getInstance()
    private val logger = Logger(this.javaClass.simpleName)
    private var userCount = 0
    private var lastSeenDisconnectedVoiceSessions = HashSet<Long>()

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

        metricsManager.updateGuildCount(guildCount)
        metricsManager.updateUserCount(userCount)
        metricsManager.updateCacheSize("markov", markovModelCache.size)
        metricsManager.updateCacheSize("subreddit", subredditCache.size)
        metricsManager.updateCacheSize("r34", r34Cache.size)
        metricsManager.updateShards(shardManager.shardManager.shardsRunning, shardManager.shardManager.shardsQueued)
        metricsManager.updateLLStats()

        try {
            metricsManager.updateDailyActiveUsers(userDAO.getActiveUsersInLastDays(1))
            metricsManager.updateWeeklyActiveUsers(userDAO.getActiveUsersInLastDays(7))
            metricsManager.updateMonthlyActiveUsers(userDAO.getActiveUsersInLastDays(30))

            metricsManager.updateDailyActiveGuilds(guildDAO.getActiveGuildsInLastDays(1))
            metricsManager.updateWeeklyActiveGuilds(guildDAO.getActiveGuildsInLastDays(7))
            metricsManager.updateMonthlyActiveGuilds(guildDAO.getActiveGuildsInLastDays(30))

            metricsManager.updateDailyActiveVoiceGuilds(guildDAO.getActiveVoiceGuildsInLastDays(1))
            metricsManager.updateWeeklyActiveVoiceGuilds(guildDAO.getActiveVoiceGuildsInLastDays(7))
            metricsManager.updateMonthlyActiveVoiceGuilds(guildDAO.getActiveVoiceGuildsInLastDays(30))
        } catch (e: Exception) {
            logger.warning("Failed to capture active guild/user counts", e)
        }

        // We need to set this status after the sharding manager is built. This will ensure that it is set to this, not the default
        shardManager.shardManager.setActivity(Activity.playing("@Vinny help"))

        val voiceConnections = GuildVoiceProvider.getInstance().getAll()
        var usersInVoice = 0
        var queuedTracks = 0
        var countWithoutOauth = 0
        var countConnected = 0
        var radioPlayers = 0
        val lavaLinkClient = LavaLinkClient.getInstance()
        val disconnectedSessions = HashSet<Long>()
        for (conn in voiceConnections) {
            val link = lavaLinkClient.client.getLinkIfCached(conn.guild.idLong)
            if (link == null) {
                disconnectedSessions.add(conn.guild.idLong)
                continue
            }
            if (conn.isConnected() && conn.guild.selfMember.voiceState!!.channel != null) {
                usersInVoice += conn.guild.selfMember.voiceState!!.channel!!.members.size - 1
                queuedTracks += (1 + conn.getQueuedTracks().size)
                metricsManager.markConnectionAge(conn.getAge(), link.node.name)
                metricsManager.markActiveVoiceConnection(link.node.name, conn.region?.name ?: "unknown", conn.autoplay, conn.isRadio())
            } else {
                metricsManager.markIdleVoiceConnection(link.node.name, conn.region?.name ?: "unknown")
            }
            if (conn.oauthConfig == null) {
                countWithoutOauth++
            }
            if (conn.isConnected()) {
                countConnected++
            } else {
                disconnectedSessions.add(conn.guild.idLong)
            }
            if (conn.isRadio()) {
                radioPlayers++
            }
        }

        // clean up disconnected sessions we have seen disconnected twice in a row
        lastSeenDisconnectedVoiceSessions.retainAll(disconnectedSessions)
        for (id in lastSeenDisconnectedVoiceSessions) {
            val conn = GuildVoiceProvider.getInstance().getGuildVoiceConnection(id)
            if (conn != null && !conn.isConnected()) {
                conn.cleanupPlayer()
                metricsManager.markForcedCleanupConnection()
            }
        }
        lastSeenDisconnectedVoiceSessions = disconnectedSessions

        metricsManager.updateConnOauth(voiceConnections.size - countWithoutOauth, countWithoutOauth)
        metricsManager.updateConnConnections(voiceConnections.size - countConnected, countConnected)
        metricsManager.updateUsersInVoice(usersInVoice)
        metricsManager.updateVoiceConnectionEntities(voiceConnections.size)
        metricsManager.updateQueuedTracks(queuedTracks)
        val healthyOauth = oauthConfigDAO.getTotalOauthConfigs(true)
        val unhealthyOauth = oauthConfigDAO.getTotalOauthConfigs(false)
        metricsManager.updateTotalOuathUsers(healthyOauth + unhealthyOauth)
        metricsManager.updateTotalHealthyOuathUsers(healthyOauth)
        metricsManager.updateTotalUnhealthyOuathUsers(unhealthyOauth)
        metricsManager.updateRadioPlayers(radioPlayers)

        for (node in lavaLinkClient.nodeHealth.entries) {
            metricsManager.markLLNodeHealth(node.key, node.value.getHealth().metricId)
        }
    }
}