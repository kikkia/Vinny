package com.bot.db;

import com.bot.models.InternalUser;
import com.bot.models.UsageLevel;
import com.bot.utils.Logger;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}
