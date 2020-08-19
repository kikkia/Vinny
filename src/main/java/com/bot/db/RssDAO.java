package com.bot.db;

import com.bot.db.mappers.RssChannelSubscriptionMapper;
import com.bot.db.mappers.RssSubscriptionMapper;
import com.bot.models.RssChannelSubscription;
import com.bot.models.RssProvider;
import com.bot.models.RssSubscription;
import com.bot.utils.Logger;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.MDC;

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

    public void addSubscription(RssProvider provider, String subject, String channelId, String authorId, boolean nsfw) throws SQLException {

        // Put subscription if does not exist
        try (Connection connection = write.getConnection()) {
            String PUT_SUB_QUERY = "INSERT INTO `rss_subscription` (subject, provider, lastScanAttempt, lastScanComplete, nsfw) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE subject=subject;";
            try (PreparedStatement statement = connection.prepareStatement(PUT_SUB_QUERY)) {
                statement.setString(1, subject);
                statement.setInt(2, provider.getValue());
                statement.setLong(3, System.currentTimeMillis());
                statement.setLong(4, System.currentTimeMillis());
                statement.setBoolean(5, nsfw);
                statement.execute();
            }
        }

        // Get id of existing or generated
        int id = 0;
        try (Connection connection = write.getConnection()) {
            // TODO: Seriously tho, I need to use JooQ or JPA or smth
            String GET_ID_FROM_SUBJECT_PROVIDER_QUERY = "SELECT id FROM `rss_subscription` WHERE provider = ? AND subject = ?;";
            try (PreparedStatement statement = connection.prepareStatement(GET_ID_FROM_SUBJECT_PROVIDER_QUERY)) {
                statement.setInt(1, provider.getValue());
                statement.setString(2, subject);
                try(ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        id = set.getInt(1);
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
            String PUT_CHANNEL_SUB_QUERY = "INSERT INTO `channel_rss_subscription` (rss_subscription_id, text_channel_id, author) VALUE (?,?,?);";
            try (PreparedStatement statement = connection.prepareStatement(PUT_CHANNEL_SUB_QUERY)) {
                statement.setInt(1, id);
                statement.setString(2, channelId);
                statement.setString(3, authorId);
                statement.execute();
            }
        }
    }

    public RssSubscription getById(int id) throws SQLException {
        try (Connection connection = write.getConnection()){
            String GET_SUBSCRIPTION_BY_ID_QUERY = "SELECT * FROM `rss_subscription` WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(GET_SUBSCRIPTION_BY_ID_QUERY)){
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

    public RssSubscription getBySubjectAndProvider(String subject, int provider) throws SQLException {
        try (Connection connection = write.getConnection()){
            String GET_SUBSCRIPTION_BY_ID_QUERY = "SELECT * FROM `rss_subscription` WHERE subject = ? AND provider = ?;";
            try (PreparedStatement statement = connection.prepareStatement(GET_SUBSCRIPTION_BY_ID_QUERY)){
                statement.setString(1, subject);
                statement.setInt(2, provider);
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
        String GET_CHANNEL_SUBS_BY_AUTHOR_QUERY = "SELECT * FROM `channel_rss_subscription` WHERE author = ?";
        return getSubscriptions(author, GET_CHANNEL_SUBS_BY_AUTHOR_QUERY);
    }

    public List<RssChannelSubscription> getSubscriptionsForChannel(String channelId) throws SQLException {
        String GET_CHANNEL_SUBS_BY_CHANNEL_QUERY = "SELECT * FROM `channel_rss_subscription` WHERE text_channel_id = ?";
        return getSubscriptions(channelId, GET_CHANNEL_SUBS_BY_CHANNEL_QUERY);
    }

    public RssChannelSubscription getChannelSubById(int id) throws SQLException {
        String GET_CHANNEL_SUB_BY_ID_QUERY = "SELECT * FROM `channel_rss_subscription` WHERE id = ?";
        try (Connection connection = write.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(GET_CHANNEL_SUB_BY_ID_QUERY)) {
                preparedStatement.setInt(1, id);
                try (ResultSet set = preparedStatement.executeQuery()) {
                    if (set.next()) {
                        RssSubscription subscription = getById(set.getInt("rss_subscription_id"));
                        return RssChannelSubscriptionMapper.mapToRssSubscription(set, subscription);
                    } else {
                        return null;
                    }
                }
            }
        }
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

    public int getCountForAuthor(String author) throws SQLException {
        try (Connection connection = write.getConnection()) {
            String getCountByUserQuery = "SELECT(*) FROM `channel_rss_subscription` WHERE author = ?";
            try (PreparedStatement statement = connection.prepareStatement(getCountByUserQuery)) {
                statement.setString(1, author);
                try (ResultSet set = statement.executeQuery()) {
                    return set.getInt(1);
                }
            }
        }
    }

    public void removeChannelSubscription(RssChannelSubscription subscription) throws SQLException {
        try (Connection connection = write.getConnection()) {
            String REMOVE_CHANNEL_SUB_BY_ID_QUERY = "DELETE FROM `channel_rss_subscription` WHERE id=?;";
            try (PreparedStatement statement = connection.prepareStatement(REMOVE_CHANNEL_SUB_BY_ID_QUERY)) {
                statement.setInt(1, subscription.getId());
                statement.execute();
            }
        }

        if (getCountForSubId(subscription.getRssSubscription().getId()) == 0) {
            removeSubscription(subscription.getRssSubscription());
        }
    }

    private void removeSubscription(RssSubscription subscription) throws SQLException {
        try (Connection connection = write.getConnection()) {
            String REMOVE_SUB_QUERY = "DELETE FROM `rss_subscription` WHERE id=?";
            try (PreparedStatement statement = connection.prepareStatement(REMOVE_SUB_QUERY)) {
                statement.setInt(1, subscription.getId());
                statement.execute();
            }
        }
        try (MDC.MDCCloseable sub_id = MDC.putCloseable("sub_id", subscription.getId() + "");
             MDC.MDCCloseable sub = MDC.putCloseable("subject", subscription.getSubject());
             MDC.MDCCloseable pro = MDC.putCloseable("provider", subscription.getProvider() + "")) {
            LOGGER.info("Removed subscription");
        }
    }

    private int getCountForSubId(int id) throws SQLException {
        try (Connection connection = write.getConnection()) {
            String COUNT_CHANNEL_SUB_BY_SUB_ID_QUERY = "SELECT COUNT(*) FROM `channel_rss_subscription` c JOIN `rss_subscription` r " +
                    "ON r.id = c.rss_subscription_id WHERE r.id = ?;";
            try (PreparedStatement statement = connection.prepareStatement(COUNT_CHANNEL_SUB_BY_SUB_ID_QUERY)){
                statement.setInt(1, id);
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next())
                        return set.getInt(1);
                    else
                        return 1; // If query barfs, keep it just in case
                }
            }
        }
    }
}
