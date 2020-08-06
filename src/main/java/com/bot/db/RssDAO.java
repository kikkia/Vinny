package com.bot.db;

import com.bot.models.RssProvider;
import com.bot.utils.Logger;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    public void addSubscription(RssProvider provider, String subject, String channelId, String authorId) throws SQLException {
        String get = "SELECT id FROM `rss_subscription` WHERE provider = ? AND url = ?;";
        String putSubscription = "INSERT INTO rss_subscription (subject, provider, lastScanAttempt, lastScanComplete) VALUES (?,?,?,?);";
        String putChannelJoin = "INSERT INTO channel_rss_subscription (rss_subscription_id, text_channel_id, author) VALUE (?,?,?);";

        // Check if the subscription currently exists
        try(Connection connection = write.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(get)) {
                
            }
        }
    }


}
