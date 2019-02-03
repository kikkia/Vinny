package com.bot.db;

import com.bot.models.InternalGuild;
import com.bot.utils.GuildUtils;
import net.dv8tion.jda.core.entities.Guild;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuildDAO {
    private static final Logger LOGGER = Logger.getLogger(PlaylistDAO.class.getName());
    private final int DEFAULT_VOLUME = 100;

    private Connection read;
    private Connection write;
    private static GuildDAO instance;


    private GuildDAO() {
        try {
            initialize();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // This constructor is only to be used by integration tests so we can pass in a connection to the integration-db
    public GuildDAO(Connection connection) {
        read = connection;
        write = connection;
    }

    public static GuildDAO getInstance() {
        if (instance == null)
            instance = new GuildDAO();
        return instance;
    }

    private void initialize() throws SQLException {
        this.read = ReadConnectionPool.getDataSource().getConnection();
        this.write = ConnectionPool.getDataSource().getConnection();
    }

    public InternalGuild getGuildById(String guildId) {
        String query = "SELECT id, name, default_volume, min_base_role_id, min_mod_role_id, min_nsfw_role_id, min_voice_role_id FROM guild WHERE id = ?";
        ResultSet set = null;
        InternalGuild returned = null;
        try {
            set = executeGetQuery(query, guildId);
            if (set.next()) {
                returned = mapSetToGuild(set);
            }
            close(null, set);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get guild from db: " + guildId, e.getSQLState());
            return null;
        }
        return returned;
    }

    // We throw on this one so if we cant add a guild to the db we just leave the guild to avoid greater problems
    public boolean addGuild(Guild guild) {
        String query = "INSERT INTO guild(id, name, default_volume, min_base_role_id, min_mod_role_id, min_nsfw_role_id, min_voice_role_id) VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=name";
        try {
            PreparedStatement statement = write.prepareStatement(query);
            statement.setString(1, guild.getId());
            statement.setString(2, guild.getName());
            statement.setInt(3, DEFAULT_VOLUME);

            statement.setString(4, guild.getPublicRole().getId());
            statement.setString(5, GuildUtils.getHighestRole(guild).getId());
            statement.setString(6, guild.getPublicRole().getId());
            statement.setString(7, guild.getPublicRole().getId());
            statement.execute();
            close(statement, null);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add guild to db: " + guild.getId(), e.getSQLState());
            return false;
        }
    }

    public void updateGuildVolume(String guildId, int newVolume) {
        try {
            String query = "UPDATE guild SET default_volume = ? WHERE id = ?";
            PreparedStatement statement = write.prepareStatement(query);
            statement.setInt(1, newVolume);
            statement.setString(2, guildId);
            statement.execute();
            close(statement, null);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update volume for guild: " + guildId, e.getSQLState());
        }
    }

    public void updateMinBaseRole(String guildId, String newRoleId) {
        try {
            String query = "UPDATE guild SET min_base_role_id = ? WHERE id = ?";
            PreparedStatement statement = write.prepareStatement(query);
            statement.setString(1, newRoleId);
            statement.setString(2, guildId);
            statement.execute();
            close(statement, null);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update base role for guild: " + guildId, e.getSQLState());
        }
    }

    public void updateMinNSFWRole(String guildId, String newRoleId) {
        try {
            String query = "UPDATE guild SET min_nsfw_role_id = ? WHERE id = ?";
            PreparedStatement statement = write.prepareStatement(query);
            statement.setString(1, newRoleId);
            statement.setString(2, guildId);
            statement.execute();
            close(statement, null);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update nsfw role for: " + guildId, e.getSQLState());
        }
    }

    public void updateMinVoiceRole(String guildId, String newRoleId) {
        try {
            String query = "UPDATE guild SET min_voice_role_id = ? WHERE id = ?";
            PreparedStatement statement = write.prepareStatement(query);
            statement.setString(1, newRoleId);
            statement.setString(2, guildId);
            statement.execute();
            close(statement, null);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update voice role for guild: " + guildId, e.getSQLState());
        }
    }

    public void updateMinModRole(String guildId, String newRoleId) {
        try {
            String query = "UPDATE guild SET min_mod_role_id = ? WHERE id = ?";
            PreparedStatement statement = write.prepareStatement(query);
            statement.setString(1, newRoleId);
            statement.setString(2, guildId);
            statement.execute();
            close(statement, null);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update min mod role for guild: " + guildId, e.getSQLState());
        }
    }

    private ResultSet executeGetQuery(String query, String guildId) throws SQLException {
        PreparedStatement statement = read.prepareStatement(query);
        statement.setString(1, guildId);
        ResultSet set = statement.executeQuery();
        close(statement, null);
        return set;
    }

    private InternalGuild mapSetToGuild(ResultSet set) throws SQLException {
        return new InternalGuild(set.getString("id"),
                set.getString("name"),
                set.getInt("default_volume"),
                set.getString("min_base_role_id"),
                set.getString("min_mod_role_id"),
                set.getString("min_nsfw_role_id"),
                set.getString("min_voice_role_id"));
    }

    private void close(PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (preparedStatement != null)
                preparedStatement.close();
            if (resultSet != null)
                resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
