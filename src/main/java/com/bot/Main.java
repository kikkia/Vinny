package com.bot;

import java.awt.*;

public class Main {
	private static ShardingManager shardingManager;
	private static final Color vinnyColor = new Color(0, 140, 186);

	public static void main(String[] args) throws Exception {
		// Sharding manager connects to the Discord API
		int numShards = Integer.parseInt(Config.getInstance().getConfig(Config.NUM_SHARDS));
		shardingManager = new ShardingManager(numShards, false);

		System.out.println("Successfully started.");
	}

}
