package com.bot;

import java.awt.*;

public class Main {
	private static ShardingManager shardingManager;
	private static String nickName;
	private static String avatarURL;
	private static final Color vinnyColor = new Color(0, 140, 186);
	private static final int NUM_SHARDS = 1;

	public static void main(String[] args) throws Exception {
		// Config gets tokens
		Config config = new Config();
		// Sharding manager connects to the Discord API
		shardingManager = new ShardingManager(NUM_SHARDS, config);

		// Getting our bots name and avatar for no real reason
		nickName = shardingManager.getJDA(0).getSelfUser().getName();
		avatarURL = shardingManager.getJDA(0).getSelfUser().getAvatarUrl();

		System.out.println("Successfully started.");
	}

}
