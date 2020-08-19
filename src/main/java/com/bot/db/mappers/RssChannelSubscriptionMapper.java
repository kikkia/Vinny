package com.bot.db.mappers;

import com.bot.models.RssChannelSubscription;
import com.bot.models.RssSubscription;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RssChannelSubscriptionMapper {
    public static RssChannelSubscription mapToRssSubscription(ResultSet set, RssSubscription subscription) throws SQLException {
        return new RssChannelSubscription(
                set.getInt("id"),
                subscription,
                set.getString("text_channel_id"),
                set.getString("author")
        );
    }
}
