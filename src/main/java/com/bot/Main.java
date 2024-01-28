package com.bot;

import com.bot.db.ConnectionPool;
import com.bot.messaging.RssSubscriber;
import com.bot.tasks.MetricsReporter;
import com.bot.tasks.RunScheduledCommandsDefferedTask;
import com.bot.utils.VinnyConfig;
import com.bot.voice.LavaLinkClient;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());


	public static void main(String[] args) throws Exception {
		// Sharding manager connects to the Discord API
		VinnyConfig config = VinnyConfig.Companion.instance();

		LOGGER.log(Level.INFO, "Hikari pool successfully initialized");
		Flyway flyway = new Flyway();
		flyway.setDataSource(ConnectionPool.getDataSource());
		try {
			flyway.migrate();
		} catch (FlywayException e) {
			System.out.println("Flyway exception found: " + e.getMessage());
			flyway.repair();
			flyway.migrate();
		}
		LOGGER.log(Level.INFO, "Flyway migrations completed");

		// Start the shards on this instance and therefore the bot
		int numShards = config.getShardingConfig().getTotal();
		int startShardIndex = config.getShardingConfig().getLocalStart();
		int endShardIndex = config.getShardingConfig().getLocalEnd();
		ShardingManager shardingManager = ShardingManager.getInstance(numShards, startShardIndex, endShardIndex);

		// Start a metrics reporter to keeps the metrics that are not frequently updates flowing to datadog
		ScheduledExecutorService scheduledTaskExecutor = Executors.newScheduledThreadPool(3);
		scheduledTaskExecutor.scheduleAtFixedRate(new MetricsReporter(), 1, 2, TimeUnit.MINUTES);

		if (config.getBotConfig().getEnableScheduledCommands()) {
			scheduledTaskExecutor.scheduleAtFixedRate(new RunScheduledCommandsDefferedTask(), 300, 9, TimeUnit.SECONDS);
		}

		// If nats is enabled, register subscribers
		if (config.getRssConfig().getEnable()) {
			new RssSubscriber(config);
		}

		LavaLinkClient.Companion.getInstance();

		System.out.println("Successfully started.");
	}

}
