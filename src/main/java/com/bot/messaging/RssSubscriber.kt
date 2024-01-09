package com.bot.messaging

import com.bot.ShardingManager
import com.bot.metrics.MetricsManager
import com.bot.models.RssProvider
import com.bot.utils.Config
import com.bot.utils.Logger
import com.bot.utils.RssUtils
import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class RssSubscriber(config: Config) {
    val logger : Logger = Logger(this.javaClass.simpleName)
    val shardingManager: ShardingManager = ShardingManager.getInstance()
    val metricsManager = MetricsManager.instance!!

    val natsConnection: Connection = Nats.connect(Options.Builder()
            .server(config.getConfig(Config.NATS_SERVER))
            .token(config.getConfig(Config.NATS_TOKEN).toCharArray())
            .build())

    init {
        val rssDispatcher = natsConnection.createDispatcher { msg ->
            val json = JSONObject(String(msg.data, StandardCharsets.UTF_8))
            //logger.info(json.toString())
            val update = RssUtils.mapJsonToUpdate(json)
            metricsManager.markRssEventReceived(RssProvider.getProvider(update.provider))

            // Send the update to the channel
            val jda = shardingManager.getShardForChannel(update.channel)
            if (jda == null) {
                logger.warning("Could not find channel for rss update ${update.channel}")
                metricsManager.markRssEventChannelNotFound(RssProvider.getProvider(update.provider), update.channel)
            } else {
                RssUtils.sendRssUpdate(update, jda)
            }
        }
        rssDispatcher.subscribe(config.getConfig(Config.RSS_SUBJECT))
    }

}