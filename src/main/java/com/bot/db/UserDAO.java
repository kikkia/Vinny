package com.bot.db;

import com.bot.db.mappers.UserMapper;
import com.bot.models.InternalUser;
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
        String query = "SELECT * FROM `user` WHERE id = ?";
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(query)){
                statement.setString(1, id);
                try (ResultSet set = statement.executeQuery()){
                    return UserMapper.mapSetToUser(set);
                }
            }
        }
    }
}
