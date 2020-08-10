package com.bot.db;

import com.bot.db.mappers.RssChannelSubscriptionMapper;
import com.bot.db.mappers.RssSubscriptionMapper;
import com.bot.models.RssChannelSubscription;
import com.bot.models.RssProvider;
import com.bot.models.RssSubscription;
import com.bot.utils.Logger;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RssDAO {
    private static final Logger LOGGER = new Logger(RssDAO.class.getName());

    private HikariDataSource write;
    private static RssDAO instance;

    String get_id_by_pro_and_sub = "SELECT id FROM `rss_subscription` WHERE provider = ? AND subject = ?;";
    String get_by_id = "SELECT * FROM `rss_subscription` WHERE id = ?";
    String get_by_author = "SELECT * FROM `channel_rss_subscription` WHERE author = ?";
    String get_by_channel = "SELECT * FROM `channel_rss_subscription` WHERE channel = ?";
    String putSubscription = "INSERT INTO rss_subscription (subject, provider, lastScanAttempt, lastScanComplete) VALUES (?,?,?,?);";
    String putChannelJoin = "INSERT INTO channel_rss_subscription (rss_subscription_id, text_channel_id, author) VALUE (?,?,?);";

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

        // Put subscription if does not exist
        try (Connection connection = write.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(putSubscription)) {
                statement.setString(1, subject);
                statement.setInt(2, provider.getValue());
                statement.setLong(3, System.currentTimeMillis());
                statement.setLong(4, System.currentTimeMillis());
                statement.execute();
            }
        }

        // Get id of existing or generated
        int id = 0;
        try (Connection connection = write.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(get_id_by_pro_and_sub)) {
                statement.setInt(1, provider.getValue());
                statement.setString(2, subject);
                try(ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        id = set.getInt(0);
                    }
                }
            }
        }

        // check that we got an id back
        if (id == 0) {
            throw new SQLException("Something went wrong inserting subscription");
        }

        // Put channel subscription
        try (Connection connection = write.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(putChannelJoin)) {
                statement.setInt(1, id);
                statement.setString(2, channelId);
                statement.setString(3, authorId);
                statement.execute();
            }
        }
    }

    public RssSubscription getById(int id) throws SQLException {
        try (Connection connection = write.getConnection()){
            try (PreparedStatement statement = connection.prepareStatement(get_by_id)){
                statement.setInt(1, id);
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        return RssSubscriptionMapper.mapSetToRssSubscription(set);
                    }
                }
            }
        }
        return null;
    }

    public List<RssChannelSubscription> getSubscriptionsForAuthor(String author) throws SQLException {
        return getSubscriptions(author, get_by_author);
    }

    public List<RssChannelSubscription> getSubscriptionsForChannel(String channelId) throws SQLException {
        return getSubscriptions(channelId, get_by_channel);
    }

    private List<RssChannelSubscription> getSubscriptions(String id, String query) throws SQLException {
        List<RssChannelSubscription> subs = new ArrayList<>();
        try (Connection connection = write.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, id);
                try (ResultSet set = statement.executeQuery()) {
                    while (set.next()) {
                        RssSubscription subscription = getById(set.getInt("rss_subscription_id"));
                        subs.add(RssChannelSubscriptionMapper.mapToRssSubscription(set, subscription));
                    }
                }
            }
        }
        return subs;
    }
}
