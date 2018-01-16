package com.bot;

import java.awt.*;

public class Main {
	private static ShardingManager shardingManager;
	private static final Color vinnyColor = new Color(0, 140, 186);
	private static final int NUM_SHARDS = 1;

	public static void main(String[] args) throws Exception {
		// Sharding manager connects to the Discord API
		shardingManager = new ShardingManager(NUM_SHARDS, false);

		System.out.println("Successfully started.");
	}

}
