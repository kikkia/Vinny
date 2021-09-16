package com.bot.db;


import com.bot.db.mappers.GuildMembershipMapper;
import com.bot.models.InternalGuildMembership;
import com.bot.utils.DbHelpers;
import com.bot.utils.Logger;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Service
public class MembershipDAO {
    private static final Logger LOGGER = new Logger(MembershipDAO.class.getName());

    private HikariDataSource write;

    // This constructor is only to be used by integration tests so we can pass in a connection to the integration-db
    public MembershipDAO(HikariDataSource dataSource) {
        write = dataSource;
    }

    public InternalGuildMembership getUserMembershipByIdInGuild(String userId, String guildId) throws SQLException {
        String query = "SELECT * FROM guild_membership gm WHERE gm.user_id = ? AND gm.guild = ?";
        InternalGuildMembership membership = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;

        try {
            connection = write.getConnection();

            statement = connection.prepareStatement(query);
            statement.setString(1, userId);
            statement.setString(2, guildId);
            set = statement.executeQuery();
            if (set.next()) {
                membership = GuildMembershipMapper.mapGuildMembership(set);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to get user membership in guild", e);
        } finally {
            DbHelpers.INSTANCE.close(statement, set, connection);
        }

        return membership;
    }

    public List<InternalGuildMembership> getMembershipsForUser(String userId) {
        String query = "SELECT u.id, u.can_use_bot, u.name, g.id FROM users u JOIN guild_membership gm ON gm.user_id = u.id JOIN guild g ON g.id = gm.guild WHERE u.id = ?";
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet set = null;
        List<InternalGuildMembership> memberships = null;

        try {
            connection = write.getConnection();

            statement = connection.prepareStatement(query);
            statement.setString(1, userId);
            set = statement.executeQuery();
            memberships = new ArrayList<>();
            while (set.next()) {
                memberships.add(GuildMembershipMapper.mapGuildMembership(set));
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to get memberships for user.", e);
        } finally {
            DbHelpers.INSTANCE.close(statement, set, connection);
        }
        return memberships;
    }

    public void removeUserMembershipToGuild(String userId, String guildId) {
        String query = "DELETE FROM guild_membership WHERE guild = ? AND user_id = ?";
        PreparedStatement statement = null;
        Connection connection = null;
        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, guildId);
            statement.setString(2, userId);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to remove user membership from guild. " + e.getMessage());
        } finally {
            DbHelpers.INSTANCE.close(statement, null, connection);
        }
    }

    public void addUserToGuild(User user, Guild guild) {
        String membershipInsertQuery = "INSERT INTO guild_membership (guild, user_id, can_use_bot) VALUES(?,?,?) ON DUPLICATE KEY UPDATE user_id = user_id";
        ensureUserExists(user);
        addMembership(membershipInsertQuery, user, guild);
    }

    public void ensureUserExists(User user) {
        String userInsertQuery = "INSERT INTO users (id, name) VALUES(?,?) ON DUPLICATE KEY UPDATE name = name";
        addUser(userInsertQuery, user);
    }

    private void addMembership(String membershipInsertQuery, User user, Guild guild) {
        PreparedStatement statement = null;
        Connection connection = null;
        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(membershipInsertQuery);
            statement.setString(1, guild.getId());
            statement.setString(2, user.getId());
            statement.setBoolean(3, true);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add membership for user to guild: " + e.getMessage());
        } finally {
            DbHelpers.INSTANCE.close(statement, null, connection);
        }
    }

    private void addUser(String userInseryQuery, User user) {
        PreparedStatement statement = null;
        Connection connection = null;
        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(userInseryQuery);
            statement.setString(1, user.getId());
            statement.setString(2, user.getName());
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add user to db: " +e.getMessage());
        } finally {
            DbHelpers.INSTANCE.close(statement, null, connection);
        }
    }

    public int getActiveUserCount() throws SQLException {
        String query = "select count(1) from guild_membership gm JOIN guild g on gm.guild = g.id WHERE g.active = 1;";
        try (Connection connection = write.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)){
                try (ResultSet set = statement.executeQuery()){
                    set.next();
                    return set.getInt(1);
                }
            }
        }
    }
}
