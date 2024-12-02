package com.bot.db;

import com.bot.ShardingManager;
import com.bot.utils.GuildUtils;
import com.bot.utils.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import static com.bot.utils.FormattingUtils.msToMinSec;

public class DataLoader {
	private final Logger LOGGER = new Logger(DataLoader.class.getName());

	private ShardingManager shardingManager;
	// Needs shards for when running on PROD

	public DataLoader() throws Exception {
		// Config gets tokens
		long startTime = System.currentTimeMillis();

		try {
			for (JDA jda: shardingManager.shardManager.getShards()){
				LoadThread thread = new LoadThread(jda, startTime);
				thread.start();
			}
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
		}

	}

	public static class LoadThread extends Thread {

		private final JDA bot;
		private final Connection connection;
		private final long startTime;
		private final String guildInsertQuery = "INSERT INTO guild(id, name, default_volume, min_base_role_id, min_mod_role_id, min_nsfw_role_id, min_voice_role_id, active) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE active=1";
		private final String textChannelInsertQuery = "INSERT INTO text_channel (id, guild, name) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE id = id";
		private final String userInsertQuery = "INSERT INTO users (id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE id = id";
		private final String guildMembershipInsertQuery = "INSERT INTO guild_membership (user_id, guild, can_use_bot) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE guild = guild";
		private final String voiceChannelInsertQuery = "INSERT INTO voice_channel (id, guild, name) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE id = id";
		private final Logger LOGGER = new Logger(LoadThread.class.getName());

		public LoadThread(JDA bot, long startTime) throws SQLException {
			this.bot = bot;
			this.startTime = startTime;
			this.connection = ConnectionPool.getDataSource().getConnection();
		}

		@Override
		public void run() {
			try {
				bot.awaitReady();
				LOGGER.log(Level.INFO, "Starting shard: " + bot.getShardInfo().getShardId() + " for " + bot.getGuilds().size() + " guilds");
				PreparedStatement statement;
				int guildCount = 0;
				int textChannelCount = 0;
				int voiceChannelCount = 0;
				int userCount = 0;
				int membershipCount = 0;

				// Loads guilds into db
				for (Guild g : bot.getGuilds()) {
					statement = connection.prepareStatement(guildInsertQuery);
					statement.setString(1, g.getId());
					statement.setString(2, g.getName());
					statement.setInt(3, 100); // default all guilds to 100
					statement.setString(4, g.getPublicRole().getId()); // Guild id and @everyone role id are shared
					statement.setString(5, GuildUtils.getHighestRole(g).getId());
					statement.setString(6, g.getPublicRole().getId());
					statement.setString(7, g.getPublicRole().getId());
					statement.setBoolean(8, true);

					statement.execute();
					statement.close();
					guildCount++;

					// Load all text channels for the guild
					for (TextChannel c : g.getTextChannels()) {
						statement = connection.prepareStatement(textChannelInsertQuery);
						statement.setString(1, c.getId());
						statement.setString(2, g.getId());
						statement.setString(3, c.getName());
						statement.execute();
						statement.close();
						textChannelCount++;

					}

					for (VoiceChannel v : g.getVoiceChannels()) {
						statement = connection.prepareStatement(voiceChannelInsertQuery);
						statement.setString(1, v.getId());
						statement.setString(2, g.getId());
						statement.setString(3, v.getName());
						statement.execute();
						statement.close();
						voiceChannelCount++;
					}

					for (Member m : g.getMembers()) {
						statement = connection.prepareStatement(userInsertQuery);
						statement.setString(1, m.getUser().getId());
						statement.setString(2, m.getUser().getName());
						statement.execute();
						statement.close();
						userCount++;

						statement = connection.prepareStatement(guildMembershipInsertQuery);
						statement.setString(1, m.getUser().getId());
						statement.setString(2, g.getId());
						statement.setBoolean(3, true);
						statement.execute();
						statement.close();
						membershipCount++;
					}
				}
				LOGGER.info("Shard: " + bot.getShardInfo().getShardId() + " Added " + guildCount + " guilds and " + textChannelCount + " channels and " + voiceChannelCount + " voice channels.");

				LOGGER.info("FINISHED: Shard: " + bot.getShardInfo().getShardId() + " Added " + userCount + " users and " + membershipCount + " memberships.");
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
				LOGGER.info("Shard: " + bot.getShardInfo().getShardId() + " Successfully migrated. Elapsed Time: " + msToMinSec(System.currentTimeMillis() - startTime));
			}
		}
	}

}
