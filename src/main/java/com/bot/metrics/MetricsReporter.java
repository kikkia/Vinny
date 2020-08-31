package com.bot.metrics;

import com.bot.ShardingManager;
import com.bot.caching.MarkovModelCache;
import com.bot.caching.R34Cache;
import com.bot.caching.SubredditCache;
import com.bot.db.MembershipDAO;
import com.bot.models.InternalShard;
import com.bot.utils.Logger;
import net.dv8tion.jda.api.entities.Activity;

/**
 * Thread that just reports some less active stats every 5 seconds
 */
public class MetricsReporter extends Thread {

    private ShardingManager shardManager;
    private MarkovModelCache markovModelCache;
    private MetricsManager metricsManager;
    private SubredditCache subredditCache;
    private R34Cache r34Cache;
    private MembershipDAO membershipDAO;
    private Logger logger = new Logger(this.getClass().getSimpleName());

    private int userCount = 0;

    public MetricsReporter() {
        shardManager = ShardingManager.getInstance();
        markovModelCache = MarkovModelCache.getInstance();
        metricsManager = MetricsManager.getInstance();
        subredditCache = SubredditCache.getInstance();
        r34Cache = R34Cache.getInstance();
        membershipDAO = MembershipDAO.getInstance();
    }

    @Override
    public void run() {
        try {
            updateMetrics();
        } catch (Exception e) {
            logger.severe("Exception when updating metrics", e);
        }
    }

    private void updateMetrics() {
        int activeVoiceConnectionCount = 0;
        int idleVoiceConnectionCount = 0;
        int totalQueuedTracks = 0;
        int totalVoiceUsers = 0;
        int guildCount = 0;

        for (InternalShard shard : shardManager.getShards().values()) {
            shard.updateStatistics();

            activeVoiceConnectionCount += shard.getActiveVoiceConnectionsCount();
            idleVoiceConnectionCount += shard.getIdleVoiceConnectionCount();
            totalQueuedTracks += shard.getQueuedTracksCount();
            totalVoiceUsers += shard.getUsersInVoiceCount();
            guildCount += shard.getServerCount();

            metricsManager.updatePing(shard.getId(), shard.getJda().getGatewayPing());
        }

        try {
            userCount = membershipDAO.getActiveUserCount();
        } catch (Exception e) {
            logger.warning("Failed to get user count", e);
        }

        metricsManager.updateActiveVoiceConnectionsCount(activeVoiceConnectionCount);
        metricsManager.updateIdleVoiceConnectionsCount(idleVoiceConnectionCount);
        metricsManager.updateQueuedTracks(totalQueuedTracks);
        metricsManager.updateUsersInVoice(totalVoiceUsers);
        metricsManager.updateGuildCount(guildCount);
        metricsManager.updateUserCount(userCount);
        // TODO: get max size
        metricsManager.updateCacheSize("markov", markovModelCache.getSize(), 0);
        metricsManager.updateCacheSize("subreddit", subredditCache.getSize(), 0);
        metricsManager.updateCacheSize("r34", r34Cache.getSize(), 0);

        metricsManager.updateShards(shardManager.shardManager.getShardsRunning(), shardManager.shardManager.getShardsQueued());

        // We need to set this status after the sharding manager is built. This will ensure that it is set to this, not the default
        shardManager.shardManager.setActivity(Activity.playing("@Vinny help"));
    }
}
