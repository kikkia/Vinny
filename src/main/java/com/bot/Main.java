package com.bot;

import com.bot.db.ConnectionPool;
import com.bot.metrics.MetricsReporter;
import com.bot.tasks.RunScheduledCommandsDefferedTask;
import com.bot.utils.Config;
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
		Config config = Config.getInstance();

		if (config.getConfig(Config.TOTAL_SHARDS) == null) {
			LOGGER.log(Level.SEVERE, "Num_Shards not set, exiting");
			return;
		}

		if (config.getConfig(Config.OWNER_ID) == null) {
			LOGGER.log(Level.SEVERE, "Owner_Id has not been set. Exiting... ");
			return;
		}

		if (config.getConfig(Config.DISCORD_TOKEN) == null){
			LOGGER.log(Level.SEVERE, "Discord token not set in config. Exiting...");
			return;
		}

		ConnectionPool connectionPool = ConnectionPool.getInstance();
		LOGGER.log(Level.INFO, "Hikari pool successfully initialized");
		Flyway flyway = new Flyway();
		flyway.setDataSource(connectionPool.getDataSource());
		try {
			flyway.migrate();
		} catch (FlywayException e) {
			System.out.println("Flyway exception found: " + e.getMessage());
			flyway.repair();
			flyway.migrate();
		}
		LOGGER.log(Level.INFO, "Flyway migrations completed");

		// Start the shards on this instance and therefore the bot
		int numShards = Integer.parseInt(config.getConfig(Config.TOTAL_SHARDS));
		int startShardIndex = Integer.parseInt(config.getConfig(Config.LOCAL_SHARD_START));
		int endShardIndex = Integer.parseInt(config.getConfig(Config.LOCAL_SHARD_END));
		ShardingManager shardingManager = ShardingManager.getInstance(numShards, startShardIndex, endShardIndex);

		// Start a metrics reporter to keeps the metrics that are not frequently updates flowing to datadog
		MetricsReporter metricsReporter = new MetricsReporter();
		metricsReporter.start();

		if (Boolean.parseBoolean(config.getConfig(Config.ENABLE_SCHEDULED_COMMANDS))) {
			ScheduledExecutorService scheduledTaskExecutor = Executors.newScheduledThreadPool(2);
			scheduledTaskExecutor.scheduleAtFixedRate(new RunScheduledCommandsDefferedTask(), 120, 10, TimeUnit.SECONDS);
		}

		System.out.println("Successfully started.");
	}

}
