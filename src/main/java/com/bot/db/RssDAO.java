package com.bot.db;

import com.bot.models.RssProvider;
import com.bot.utils.Logger;
import com.zaxxer.hikari.HikariDataSource;

public class RssDAO {
    private static final Logger LOGGER = new Logger(RssDAO.class.getName());

    private HikariDataSource write;
    private static RssDAO instance;

    private RssDAO() {
        initialize();
    }

    public static RssDAO getInstance() {
        if (instance == null) {
            instance = new RssDAO();
        }
        return instance;
    }

    private void initialize() {
        this.write = ConnectionPool.getDataSource();
    }

    public void addSubscription(RssProvider provider, String subject, String channelId, String authorId)

}
