package com.bot

import com.bot.db.ConnectionPool
import com.bot.messaging.RssSubscriber
import com.bot.tasks.MetricsReporter
import com.bot.tasks.RunScheduledCommandsDeferredTask
import com.bot.utils.VinnyConfig.Companion.instance
import com.bot.voice.LavaLinkClient.Companion.getInstance
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

object Main {
    private val LOGGER: Logger = Logger.getLogger(Main::class.java.name)


    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Sharding manager connects to the Discord API
        val config = instance()

        LOGGER.log(Level.INFO, "Hikari pool successfully initialized")
        val flyway = Flyway()
        flyway.dataSource = ConnectionPool.getDataSource()
        try {
            flyway.migrate()
        } catch (e: FlywayException) {
            println("Flyway exception found: " + e.message)
            flyway.repair()
            flyway.migrate()
        }
        LOGGER.log(Level.INFO, "Flyway migrations completed")

        // Start the shards on this instance and therefore the bot
        val numShards = config.shardingConfig.total
        val startShardIndex = config.shardingConfig.localStart
        val endShardIndex = config.shardingConfig.localEnd
        val shardingManager = ShardingManager.getInstance(numShards, startShardIndex, endShardIndex)

        // Start a metrics reporter to keeps the metrics that are not frequently updates flowing to datadog
        val scheduledTaskExecutor = Executors.newScheduledThreadPool(3)
        scheduledTaskExecutor.scheduleAtFixedRate(MetricsReporter(), 1, 2, TimeUnit.MINUTES)

        if (config.botConfig.enableScheduledCommands) {
            scheduledTaskExecutor.scheduleAtFixedRate(RunScheduledCommandsDeferredTask(), 360, 9, TimeUnit.SECONDS)
        }

        // If nats is enabled, register subscribers
        if (config.rssConfig.enable) {
            RssSubscriber(config)
        }

        getInstance()

        println("Successfully started.")
    }
}
