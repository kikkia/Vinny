package com.bot.db;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class UserRepository {
    private static final Logger LOGGER = Logger.getLogger(UserRepository.class.getName());

    private Connection read;
    private Connection write;
    private static UserRepository instance;

    private UserRepository() {
        try {
            initialize();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    private void initialize() throws SQLException {
        ConnectionPool connectionPool = ConnectionPool.getInstance();
        ReadConnectionPool readConnectionPool = ReadConnectionPool.getInstance();

        DataSource dataSource = connectionPool.getDataSource();
        DataSource readDataSource = readConnectionPool.getDataSource();

        this.read = readDataSource.getConnection();
        this.write = dataSource.getConnection();
    }

}
