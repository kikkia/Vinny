package com.bot;

import com.bot.db.ConnectionPool;
import org.flywaydb.core.Flyway;

public class Main {

	public static void main(String[] args) throws Exception {
		// Sharding manager connects to the Discord API
		Config config = Config.getInstance();

		if (config.getConfig(Config.NUM_SHARDS) == null) {
			System.out.println("Num_Shards not set, exiting");
		}

		if (config.getConfig(Config.OWNER_ID) == null) {
			System.out.println("Owner_Id has not been set. Exiting... ");
			return;
		}

		if (config.getConfig(Config.DISCORD_TOKEN) == null){
			System.out.println("Discord token not set in config. Exiting...");
			return;
		}

		if (Boolean.parseBoolean(Config.USE_DB)) {
			ConnectionPool connectionPool = ConnectionPool.getInstance();
			Flyway flyway = new Flyway();
			flyway.setDataSource(connectionPool.getDataSource());
			flyway.migrate();
		}

		int numShards = Integer.parseInt(config.getConfig(Config.NUM_SHARDS));
		ShardingManager shardingManager = new ShardingManager(numShards, false);

		System.out.println("Successfully started.");
	}

}
