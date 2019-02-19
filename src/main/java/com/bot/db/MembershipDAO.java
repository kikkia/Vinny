package com.bot.db;


import com.bot.models.UserMembership;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MembershipDAO {
    private static final Logger LOGGER = Logger.getLogger(MembershipDAO.class.getName());

    private Connection read;
    private Connection write;
    private static MembershipDAO instance;

    private MembershipDAO() {
        try {
            initialize();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    // This constructor is only to be used by integration tests so we can pass in a connection to the integration-db
    public MembershipDAO(Connection connection) {
        read = connection;
        write = connection;
    }


    public static MembershipDAO getInstance() {
        if (instance == null) {
            instance = new MembershipDAO();
        }
        return instance;
    }

    private void initialize() throws SQLException {
        this.read = ReadConnectionPool.getDataSource().getConnection();
        this.write = ConnectionPool.getDataSource().getConnection();
    }

    public UserMembership getUserMembershipByIdInGuild(String userId, String guildId) throws SQLException {
        // TODO: IS there a better way to close these in case of error
        String query = "SELECT u.id, u.can_use_bot, u.name, g.id FROM users u JOIN guild_membership gm ON gm.user_id = u.id JOIN guild g ON g.id = gm.guild WHERE g.id = ? AND u.id = ?";
        UserMembership membership = null;

        PreparedStatement statement = read.prepareStatement(query);
        statement.setString(1, guildId);
        statement.setString(2, userId);
        ResultSet set = statement.executeQuery();
        if (set.next()) {
            membership = mapUserMembership(set);
        }

        close(statement, set);
        return membership;
    }

    public List<UserMembership> getMembershipsForUser(String userId) throws SQLException {
        String query = "SELECT u.id, u.can_use_bot, u.name, g.id FROM users u JOIN guild_membership gm ON gm.user_id = u.id JOIN guild g ON g.id = gm.guild WHERE u.id = ?";
        PreparedStatement statement = read.prepareStatement(query);
        statement.setString(1, userId);
        ResultSet set = statement.executeQuery();
        List<UserMembership> memberships = new ArrayList<>();
        while (set.next()) {
            memberships.add(mapUserMembership(set));
        }
        close(statement, set);
        return memberships;
    }

    public void removeUserMembershipToGuild(String userId, String guildId) {
        String query = "DELETE FROM guild_membership WHERE guild = ? AND user_id = ?";
        PreparedStatement statement = null;
        try {
            statement = write.prepareStatement(query);
            statement.setString(1, guildId);
            statement.setString(2, userId);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to remove user membership from guild. " + e.getMessage());
        } finally {
            close(statement, null);
        }
    }

    public void addUserToGuild(User user, Guild guild) {
        String userInsertQuery = "INSERT INTO users (id, name) VALUES(?,?) ON DUPLICATE KEY UPDATE name = name";
        String membershipInsertQuery = "INSERT INTO guild_membership (guild, user_id) VALUES(?,?) ON DUPLICATE KEY UPDATE user_id = user_id";
        addUser(userInsertQuery, user);
        addMembership(membershipInsertQuery, user, guild);
    }

    private void addMembership(String membershipInsertQuery, User user, Guild guild) {
        PreparedStatement statement = null;
        try {
            statement = write.prepareStatement(membershipInsertQuery);
            statement.setString(1, guild.getId());
            statement.setString(2, user.getId());
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add membership for user to guild: " + e.getMessage());
        } finally {
            close(statement, null);
        }
    }

    private void addUser(String userInseryQuery, User user) {
        PreparedStatement statement = null;
        try {
            statement = write.prepareStatement(userInseryQuery);
            statement.setString(1, user.getId());
            statement.setString(2, user.getName());
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add user to db: " +e.getMessage());
        } finally {
            close(statement, null);
        }
    }

    private ResultSet executeGetQuery(String query) throws SQLException {
        PreparedStatement statement = read.prepareStatement(query);
        ResultSet set = statement.executeQuery();
        close(statement, null);
        return set;
    }

    private UserMembership mapUserMembership(ResultSet set) throws SQLException {
        return new UserMembership(set.getString("u.id"),
                set.getString("name"),
                set.getString("g.id"),
                set.getBoolean("u.can_use_bot"));
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
