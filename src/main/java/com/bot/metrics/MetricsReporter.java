package com.bot.metrics;

import com.bot.ShardingManager;
import com.bot.caching.MarkovModelCache;
import com.bot.models.InternalShard;

/**
 * Thread that just reports some less active stats every 5 seconds
 */
public class MetricsReporter extends Thread {

    private ShardingManager shardManager;
    private MarkovModelCache markovModelCache;
    private MetricsManager metricsManager;

    public MetricsReporter() {
        shardManager = ShardingManager.getInstance();
        markovModelCache = MarkovModelCache.getInstance();
        metricsManager = MetricsManager.getInstance();
    }

    @Override
    public void run() {
        while(true) {
            int activeVoiceConnectionCount = 0;
            int idleVoiceConnectionCount = 0;
            int totalQueuedTracks = 0;
            int totalVoiceUsers = 0;
            int guildCount = 0;
            int userCount = 0;

            for (InternalShard shard : shardManager.getShards().values()) {
                shard.updateStatistics();

                activeVoiceConnectionCount += shard.getActiveVoiceConnectionsCount();
                idleVoiceConnectionCount += shard.getIdleVoiceConnectionCount();
                totalQueuedTracks += shard.getQueuedTracksCount();
                totalVoiceUsers += shard.getUsersInVoiceCount();
                guildCount += shard.getServerCount();
                userCount += shard.getUserCount();

                metricsManager.updatePing(shard.getId(), shard.getJda().getPing());
            }

            metricsManager.updateActiveVoiceConnectionsCount(activeVoiceConnectionCount);
            metricsManager.updateIdleVoiceConnectionsCount(idleVoiceConnectionCount);
            metricsManager.updateQueuedTracks(totalQueuedTracks);
            metricsManager.updateUsersInVoice(totalVoiceUsers);
            metricsManager.updateGuildCount(guildCount);
            metricsManager.updateUserCount(userCount);
            // TODO: get max size
            metricsManager.updateCacheSize("markov", markovModelCache.getSize(), 0);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }
}
