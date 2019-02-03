package com.bot;

import com.bot.db.ConnectionPool;
import com.bot.db.DataLoader;
import com.bot.utils.Config;
import org.flywaydb.core.Flyway;

import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());


	public static void main(String[] args) throws Exception {
		// Sharding manager connects to the Discord API
		Config config = Config.getInstance();

		if (config.getConfig(Config.NUM_SHARDS) == null) {
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

		boolean useDB = Boolean.parseBoolean(config.getConfig(Config.USE_DB));
		if (useDB) {
			ConnectionPool connectionPool = ConnectionPool.getInstance();
			LOGGER.log(Level.INFO, "Hikari pool successfully initialized");
			Flyway flyway = new Flyway();
			flyway.setDataSource(connectionPool.getDataSource());
			flyway.migrate();
			LOGGER.log(Level.INFO, "Flyway migrations completed");
		}

		int numShards = Integer.parseInt(config.getConfig(Config.NUM_SHARDS));
		boolean dataLoader = Boolean.parseBoolean(config.getConfig(Config.DATA_LOADER));
		ShardingManager shardingManager = new ShardingManager(numShards, dataLoader, useDB);

		if(dataLoader) {
			LOGGER.log(Level.INFO, "Starting a data loader");
			DataLoader loader = new DataLoader(shardingManager);
		}

		System.out.println("Successfully started.");
	}

}
