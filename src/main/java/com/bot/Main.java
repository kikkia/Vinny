package com.bot;

import com.bot.db.PlaylistRepository;

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

		int numShards = Integer.parseInt(config.getConfig(Config.NUM_SHARDS));
		ShardingManager shardingManager = new ShardingManager(numShards, false);

		System.out.println("Successfully started.");
	}

}
