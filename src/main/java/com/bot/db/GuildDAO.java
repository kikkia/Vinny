package com.bot.db;

import com.bot.models.InternalGuild;
import net.dv8tion.jda.core.entities.Guild;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public static GuildDAO getInstance() {
        if (instance == null)
            instance = new GuildDAO();
        return instance;
    }

    private void initialize() throws SQLException {
        this.read = ReadConnectionPool.getDataSource().getConnection();
        this.write = ConnectionPool.getDataSource().getConnection();
    }

    public InternalGuild getGuildById(String guildId) throws SQLException {
        String query = "SELECT id, name, default_volume, min_base_role, min_mod_role, min_nsfw_role, min_voice_role FROM guild WHERE id = ?";
        ResultSet set = executeGetQuery(query, guildId);
        set.next();
        InternalGuild returned = mapSetToGuild(set);
        set.close();
        return returned;
    }

    // We throw on this one so if we cant add a guild to the db we just leave the guild to avoid greater problems
    public void addGuild(Guild guild) throws SQLException {
        String query = "INSERT INTO guild(id, name, default_volume, min_base_role, min_mod_role, min_nsfw_role, min_voice_role VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=name";
        PreparedStatement statement = write.prepareStatement(query);
        statement.setString(1, guild.getId());
        statement.setString(2, guild.getName());
        statement.setInt(3, DEFAULT_VOLUME);
        // Set the default roles to the @everyone role which shares id with guild
        statement.setString(4, guild.getId());
        statement.setString(5, guild.getId());
        statement.setString(6, guild.getId());
        statement.setString(7, guild.getId());
        statement.execute();
        statement.close();
    }

    public void updateGuildVolume(String guildId, int newVolume) throws SQLException {
        String query = "UPDATE guild SET default_volume = ? WHERE id = ?";
        PreparedStatement statement = write.prepareStatement(query);
        statement.setInt(1, newVolume);
        statement.setString(2, guildId);
        statement.execute();
        statement.close();
    }

    public void updateMinBaseRole(String guildId, String newRoleId) throws SQLException {
        String query = "UPDATE guild SET min_base_role = ? WHERE id = ?";
        PreparedStatement statement = write.prepareStatement(query);
        statement.setString(1, newRoleId);
        statement.setString(2, guildId);
        statement.execute();
        statement.close();
    }

    public void updateMinNSFWRole(String guildId, String newRoleId) throws SQLException {
        String query = "UPDATE guild SET min_nsfw_role = ? WHERE id = ?";
        PreparedStatement statement = write.prepareStatement(query);
        statement.setString(1, newRoleId);
        statement.setString(2, guildId);
        statement.execute();
        statement.close();
    }

    public void updateMinVoiceRole(String guildId, String newRoleId) throws SQLException {
        String query = "UPDATE guild SET min_voice_role = ? WHERE id = ?";
        PreparedStatement statement = write.prepareStatement(query);
        statement.setString(1, newRoleId);
        statement.setString(2, guildId);
        statement.execute();
        statement.close();
    }

    public void updateMinModRole(String guildId, String newRoleId) throws SQLException {
        String query = "UPDATE guild SET min_mod_role = ? WHERE id = ?";
        PreparedStatement statement = write.prepareStatement(query);
        statement.setString(1, newRoleId);
        statement.setString(2, guildId);
        statement.execute();
        statement.close();
    }

    private ResultSet executeGetQuery(String query, String guildId) throws SQLException {
        PreparedStatement statement = read.prepareStatement(query);
        statement.setString(1, guildId);
        ResultSet set = statement.executeQuery();
        statement.close();
        return set;
    }

    private InternalGuild mapSetToGuild(ResultSet set) throws SQLException {
        return new InternalGuild(set.getString("id"),
                set.getString("name"),
                set.getInt("default_volume"),
                set.getString("min_base_role"),
                set.getString("min_mod_role"),
                set.getString("min_nsfw_role"),
                set.getString("min_voice_role"));
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
