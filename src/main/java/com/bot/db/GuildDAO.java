package com.bot.db;

import com.bot.caching.GuildCache;
import com.bot.db.mappers.GuildMapper;
import com.bot.models.InternalGuild;
import com.bot.utils.DbHelpers;
import com.bot.utils.GuildUtils;
import com.bot.utils.Logger;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class GuildDAO {
    private static final Logger LOGGER = new Logger(PlaylistDAO.class.getName());
    private final int DEFAULT_VOLUME = 100;

    private HikariDataSource write;
    private GuildCache cache;
    private AliasDAO aliasDAO;
    private static GuildDAO instance;


    private GuildDAO() {
        try {
            initialize();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // This constructor is only to be used by integration tests so we can pass in a connection to the integration-db
    public GuildDAO(HikariDataSource dataSource, AliasDAO aliasDAO) {
        this.write = dataSource;
        this.cache = GuildCache.getInstance();
        this.aliasDAO = aliasDAO;
    }

    public static GuildDAO getInstance() {
        if (instance == null)
            instance = new GuildDAO();
        return instance;
    }

    private void initialize() throws SQLException {
        this.write = ConnectionPool.getDataSource();
        this.cache = GuildCache.getInstance();
        this.aliasDAO = AliasDAO.getInstance();

    }

    public InternalGuild getGuildById(String guildId) {
        return getGuildById(guildId, true);
    }

    public InternalGuild getGuildById(String guildId, boolean useCache) {
        String query = "SELECT * FROM guild WHERE id = ?";
        InternalGuild returned = null;

        if (useCache) {
            returned = cache.get(guildId);
        }

        if (returned != null) {
            return returned;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;

        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, guildId);
            set = statement.executeQuery();
            if (set.next()) {
                returned = GuildMapper.mapSetToGuild(set);
            }
            if (returned != null) {
                returned.setAliasList(aliasDAO.getGuildAliases(guildId));
                cache.put(returned.getId(), returned);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to get guildById", e);
        } finally {
            DbHelpers.INSTANCE.close(statement, set, connection);
        }

        return returned;
    }

    public int getActiveGuildCount() {
        String query = "SELECT COUNT(*) FROM guild WHERE active=1";
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        int count = 0;

        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);
            set = statement.executeQuery();
            if (set.next()) {
                count = set.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to get guild count", e);
        } finally {
            DbHelpers.INSTANCE.close(statement, set, connection);
        }

        return count;
    }

    // We throw on this one so if we cant add a guild to the db we just leave the guild to avoid greater problems
    public boolean addGuild(Guild guild) {
        String query = "INSERT INTO guild(id, name, default_volume, min_base_role_id, min_mod_role_id, min_nsfw_role_id, min_voice_role_id, active) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE active=1";
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
            statement.setBoolean(8, true);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add guild to db: " + guild.getId() + " " + e.getMessage());
            return false;
        } finally {
            DbHelpers.INSTANCE.close(statement, null, connection);
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
            DbHelpers.INSTANCE.close(statement, null, connection);
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
            DbHelpers.INSTANCE.close(statement, null, connection);
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
            DbHelpers.INSTANCE.close(statement, null, connection);
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
            DbHelpers.INSTANCE.close(statement, null, connection);
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
            DbHelpers.INSTANCE.close(statement, null, connection);
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
            DbHelpers.INSTANCE.close(statement, null, connection);
        }

        updateGuildInCache(guildId);

        return true;
    }

    public void setGuildActive(String guildId, boolean active) {
        String query = "UPDATE guild SET active = ? WHERE id = ?";
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);
            statement.setBoolean(1, active);
            statement.setString(2, guildId);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update active flag for guild: " + guildId + " " + e.getMessage());
        } finally {
            DbHelpers.INSTANCE.close(statement, null, connection);
        }

        updateGuildInCache(guildId);
    }



    private ResultSet executeGetQuery(String query, String guildId) throws SQLException {
        Connection connection = write.getConnection();
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, guildId);
        ResultSet set = statement.executeQuery();
        DbHelpers.INSTANCE.close(statement, null, connection);
        return set;
    }

    // Whenever we update a guild we want to make sure its updated in the cache
    private void updateGuildInCache(String guildId) {
        InternalGuild guild = getGuildById(guildId, false);
        cache.put(guildId, guild);
    }

    public void updateGuildInCache(InternalGuild guild) {
        cache.put(guild.getId(), guild);
    }
}
