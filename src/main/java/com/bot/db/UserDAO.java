package com.bot.db;

import com.bot.models.InternalUser;
import com.bot.models.UsageLevel;
import com.bot.utils.DbHelpers;
import com.bot.utils.Logger;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class UserDAO {
    private static final Logger LOGGER = new Logger(RssDAO.class.getName());

    private HikariDataSource write;
    private static UserDAO instance;

    private UserDAO() {
        initialize();
    }

    public static UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }

    private void initialize() {
        this.write = ConnectionPool.getDataSource();
    }

    public InternalUser getById(String id) throws SQLException {
        try (Connection connection = write.getConnection()){
            String GET_USER_BY_ID_QUERY = "SELECT * FROM `users` WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(GET_USER_BY_ID_QUERY)){
                statement.setString(1, id);
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        return InternalUser.Companion.mapSetToUser(set);
                    }
                }
            }
        }
        return null;
    }

    public void setUsageLevel(UsageLevel level, String userId) throws SQLException {
        String query = "UPDATE users SET usage_level = ? WHERE id = ?";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)){
                statement.setInt(1, level.getId());
                statement.setString(2, userId);
                statement.execute();
            }
        }
    }

    public void updateLastCommandRanTime(String id) {
        String query = "UPDATE users SET last_command = current_timestamp() WHERE id = ?";
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);

            statement.setString(1, id);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update last command time: " + id + " " + e.getMessage());
        } finally {
            DbHelpers.INSTANCE.close(statement, null, connection);
        }
    }

    public int getActiveUsersInLastDays(int days) {
        String query = "SELECT count(*) FROM users WHERE last_command >= CURDATE() - INTERVAL ? DAY";
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = write.getConnection();
            statement = connection.prepareStatement(query);

            statement.setInt(1, days);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get active user count for: " + days + " " + e.getMessage());
        } finally {
            DbHelpers.INSTANCE.close(statement, null, connection);
        }
        return -1;
    }
}
