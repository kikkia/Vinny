package com.bot.db;

import com.bot.utils.Config;
import com.bot.ShardingManager;
import com.bot.utils.GuildUtils;
import com.bot.voice.QueuedAudioTrack;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import org.flywaydb.core.Flyway;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataLoader {
	private final Logger LOGGER = Logger.getLogger(DataLoader.class.getName());

	private ShardingManager shardingManager;
	// Needs shards for when running on PROD

	public DataLoader(ShardingManager shardingManager) throws Exception {
		this.shardingManager = shardingManager;

		// Config gets tokens
		Config config = Config.getInstance();
		long startTime = System.currentTimeMillis();

		if (config.getConfig(Config.USE_DB).equals("False")) {
			System.out.println("Use_DB Set to False in the config file. Exiting...");
			return;
		}

		if (config.getConfig(Config.DB_URI) == null) {
			System.out.println("DB_URI not set in the config file. Exiting...");
			return;
		}

		if (config.getConfig(Config.DB_USERNAME) == null) {
			System.out.println("DB_Username not set in the config file. Exiting...");
			return;
		}

		if (config.getConfig(Config.DB_PASSWORD) == null) {
			System.out.println("DB_Password not set in the config file. Exiting...");
			return;
		}

		LOGGER.log(Level.INFO, "Hikari pool successfully initialized");
		Flyway flyway = new Flyway();
		flyway.setDataSource(ConnectionPool.getDataSource());
		flyway.migrate();
		LOGGER.log(Level.INFO, "Flyway migrations completed");

		List<LoadThread> loadThreads = new ArrayList<>();
		try {
			for (JDA bot: shardingManager.getShards()){
				loadThreads.add(new LoadThread(bot, config, startTime));
			}
			for (LoadThread thread : loadThreads) {
				thread.start();
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}

	}

	private static class LoadThread extends Thread {

		private JDA bot;
		private Connection connection = null;
		private long startTime = 0;
		private String guildInsertQuery = "INSERT INTO guild(id, name, default_volume, min_base_role_id, min_mod_role_id, min_nsfw_role_id, min_voice_role_id) VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=name";
		private String textChannelInsertQuery = "INSERT INTO text_channel (id, guild, name) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE id = id";
		private String userInsertQuery = "INSERT INTO users (id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE id = id";
		private String guildMembershipInsertQuery = "INSERT INTO guild_membership (user_id, guild) VALUES (?, ?) ON DUPLICATE KEY UPDATE guild = guild";
		private String voiceChannelInsertQuery = "INSERT INTO voice_channel (id, guild, name) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE id = id";

		public LoadThread(JDA bot, Config config, long startTime) throws SQLException, ClassNotFoundException {
			this.bot = bot;
			this.startTime = startTime;
			Class.forName("com.mysql.jdbc.Driver");
			this.connection = ConnectionPool.getDataSource().getConnection();
		}

		@Override
		public void run() {
			try {
				System.out.println("Starting shard: " + bot.getShardInfo().getShardId() + " for " + bot.getGuilds().size() + " guilds");
				PreparedStatement statement;
				List<User> users = bot.getUsers();
				List<Guild> guilds = bot.getGuilds();
				int guildCount = 0;
				int textChannelCount = 0;
				int voiceChannelCount = 0;
				int userCount = 0;
				int membershipCount = 0;

				// Loads guilds into db
				for (Guild g : guilds) {
					statement = connection.prepareStatement(guildInsertQuery);
					statement.setString(1, g.getId());
					statement.setString(2, g.getName());
					statement.setInt(3, 100); // default all guilds to 100
					statement.setString(4, g.getPublicRole().getId()); // Guild id and @everyone role id are shared
					statement.setString(5, GuildUtils.getHighestRole(g).getId());
					statement.setString(6, g.getPublicRole().getId());
					statement.setString(7, g.getPublicRole().getId());

					statement.execute();
					guildCount++;

					if (guildCount % 250 == 0) {
						System.out.println("Shard: " + bot.getShardInfo().getShardId() + " Added " + guildCount + " guilds");
					}

					// Load all text channels for the guild
					for (TextChannel c : g.getTextChannels()) {
						statement = connection.prepareStatement(textChannelInsertQuery);
						statement.setString(1, c.getId());
						statement.setString(2, g.getId());
						statement.setString(3, c.getName());
						statement.execute();
						textChannelCount++;

						if (textChannelCount % 500 == 0) {
							System.out.println("Shard: " + bot.getShardInfo().getShardId() + " Added " + textChannelCount + " textChannels");
						}
					}

					for (VoiceChannel v : g.getVoiceChannels()) {
						statement = connection.prepareStatement(voiceChannelInsertQuery);
						statement.setString(1, v.getId());
						statement.setString(2, g.getId());
						statement.setString(3, v.getName());
						statement.execute();
						voiceChannelCount++;
					}

					for (Member m : g.getMembers()) {
						statement = connection.prepareStatement(userInsertQuery);
						statement.setString(1, m.getUser().getId());
						statement.setString(2, m.getUser().getName());
						statement.execute();
						userCount++;

						if (userCount % 2500 == 0) {
							System.out.println("Shard: " + bot.getShardInfo().getShardId() + " Added " + userCount + " users");
						}

						statement = connection.prepareStatement(guildMembershipInsertQuery);
						statement.setString(1, m.getUser().getId());
						statement.setString(2, g.getId());
						statement.execute();
						membershipCount++;

						if (membershipCount % 5000 == 0) {
							System.out.println("Shard: " + bot.getShardInfo().getShardId() + " Added " + membershipCount + " memberships");
						}
					}
				}
				System.out.println("Shard: " + bot.getShardInfo().getShardId() + " Added " + guildCount + " guilds and " + textChannelCount + " channels and " + voiceChannelCount + " voice channels.");


				System.out.println("Starting user and membership migration");
				System.out.println("FINISHED: Shard: " + bot.getShardInfo().getShardId() + " Added " + userCount + " users and " + membershipCount + " memberships.");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				System.out.println("Shard: " + bot.getShardInfo().getShardId() + " Successfully migrated. Elapsed Time: " + QueuedAudioTrack.msToMinSec(System.currentTimeMillis() - startTime));
			}
		}
	}

}
