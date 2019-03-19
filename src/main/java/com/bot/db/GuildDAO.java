package com.bot.db;

import com.bot.db.mappers.GuildMapper;
import com.bot.models.InternalGuild;
import com.bot.caching.GuildCache;
import com.bot.utils.DbHelpers;
import com.bot.utils.GuildUtils;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuildDAO {
    private static final Logger LOGGER = Logger.getLogger(PlaylistDAO.class.getName());
    private final int DEFAULT_VOLUME = 100;

    private HikariDataSource read;
    private HikariDataSource write;
    private GuildCache cache;
    private static GuildDAO instance;


    private GuildDAO() {
        try {
            initialize();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // This constructor is only to be used by integration tests so we can pass in a connection to the integration-db
    public GuildDAO(HikariDataSource dataSource) {
        read = dataSource;
        write = dataSource;
        cache = GuildCache.getInstance();
    }

    public static GuildDAO getInstance() {
        if (instance == null)
            instance = new GuildDAO();
        return instance;
    }

    private void initialize() throws SQLException {
        this.read = ReadConnectionPool.getDataSource();
        this.write = ConnectionPool.getDataSource();
        cache = GuildCache.getInstance();
    }

    public InternalGuild getGuildById(String guildId) {
        return getGuildById(guildId, true);
    }

    public InternalGuild getGuildById(String guildId, boolean useCache) {
        String query = "SELECT * FROM guild WHERE id = ?";
        InternalGuild returned = null;

        if (useCache)
            returned = cache.get(guildId);

        if (returned != null) {
            return returned;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;

        try {
            connection = read.getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, guildId);
            set = statement.executeQuery();
            if (set.next()) {
                returned = GuildMapper.mapSetToGuild(set);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to get guildById: " + e.getMessage());
        } finally {
            DbHelpers.close(statement, set, connection);
        }

        return returned;
    }

    // We throw on this one so if we cant add a guild to the db we just leave the guild to avoid greater problems
    public boolean addGuild(Guild guild) {
        String query = "INSERT INTO guild(id, name, default_volume, min_base_role_id, min_mod_role_id, min_nsfw_role_id, min_voice_role_id) VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=name";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, guild.getId());
            statement.setString(2, guild.getName());
            statement.setInt(3, DEFAULT_VOLUME);

            statement.setString(4, guild.getPublicRole().getId());
            statement.setString(5, GuildUtils.getHighestRole(guild).getId());
            statement.setString(6, guild.getPublicRole().getId());
            statement.setString(7, guild.getPublicRole().getId());
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add guild to db: " + guild.getId() + " " + e.getMessage());
            return false;
        } finally {
            DbHelpers.close(statement, null, connection);
        }

        updateGuildInCache(guild.getId());

        return true;
    }

    public void addFreshGuild(Guild guild) {
        MembershipDAO membershipDAO = MembershipDAO.getInstance();
        ChannelDAO channelDAO = ChannelDAO.getInstance();

        addGuild(guild);
        for (Member m : guild.getMembers()) {
            membershipDAO.addUserToGuild(m.getUser(), guild);
        }
        for (TextChannel t : guild.getTextChannels()) {
            channelDAO.addTextChannel(t);
        }
        for (VoiceChannel v : guild.getVoiceChannels()) {
            channelDAO.addVoiceChannel(v);
        }

        LOGGER.info("Completed addition of fresh guild. " + guild.getId());
    }

    public boolean updateGuildVolume(String guildId, int newVolume) {
        String query = "UPDATE guild SET default_volume = ? WHERE id = ?";

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);
            statement.setInt(1, newVolume);
            statement.setString(2, guildId);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update volume for guild: " + guildId + " " + e.getMessage());
            return false;
        } finally {
            DbHelpers.close(statement, null, connection);
        }

        updateGuildInCache(guildId);

        return true;
    }

    public boolean updateMinBaseRole(String guildId, String newRoleId) {
        String query = "UPDATE guild SET min_base_role_id = ? WHERE id = ?";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, newRoleId);
            statement.setString(2, guildId);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update base role for guild: " + guildId + " " + e.getMessage());
            return false;
        } finally {
            DbHelpers.close(statement, null, connection);
        }

        updateGuildInCache(guildId);

        return true;
    }

    public boolean updateMinNSFWRole(String guildId, String newRoleId) {
        String query = "UPDATE guild SET min_nsfw_role_id = ? WHERE id = ?";
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, newRoleId);
            statement.setString(2, guildId);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update nsfw role for: " + guildId + " " + e.getMessage());
            return false;
        } finally {
            DbHelpers.close(statement, null, connection);
        }

        updateGuildInCache(guildId);

        return true;
    }

    public boolean updateMinVoiceRole(String guildId, String newRoleId) {
        String query = "UPDATE guild SET min_voice_role_id = ? WHERE id = ?";
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, newRoleId);
            statement.setString(2, guildId);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update voice role for guild: " + guildId + " " + e.getMessage());
            return false;
        } finally {
            DbHelpers.close(statement, null, connection);
        }

        updateGuildInCache(guildId);

        return true;
    }

    public boolean updateMinModRole(String guildId, String newRoleId) {
        String query = "UPDATE guild SET min_mod_role_id = ? WHERE id = ?";
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, newRoleId);
            statement.setString(2, guildId);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update min mod role for guild: " + guildId + " " + e.getMessage());
            return false;
        } finally {
            DbHelpers.close(statement, null,connection);
        }

        updateGuildInCache(guildId);

        return true;
    }

    public boolean updateGuildPrefixes(String guildId, List<String> prefixList) {
        String prefixes = GuildUtils.convertListToPrefixesString(prefixList);
        String query = "UPDATE guild SET prefixes = ? WHERE id = ?";
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, prefixes);
            statement.setString(2, guildId);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update prefixes for guild: " + guildId + " " + e.getMessage());
            return false;
        } finally {
            DbHelpers.close(statement, null,connection);
        }

        updateGuildInCache(guildId);

        return true;
    }

    private ResultSet executeGetQuery(String query, String guildId) throws SQLException {
        Connection connection = read.getConnection();
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, guildId);
        ResultSet set = statement.executeQuery();
        DbHelpers.close(statement, null, connection);
        return set;
    }

    // Whenever we update a guild we want to make sure its updated in the cache
    private void updateGuildInCache(String guildId) {
        InternalGuild guild = getGuildById(guildId, false);
        cache.put(guildId, guild);
    }
}
