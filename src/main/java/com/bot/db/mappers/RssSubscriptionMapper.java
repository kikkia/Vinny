package com.bot.db.mappers;

import com.bot.models.RssProvider;
import com.bot.models.RssSubscription;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RssSubscriptionMapper {
    public static RssSubscription mapSetToRssSubscription(ResultSet set) throws SQLException {
        return new RssSubscription(
                set.getInt("id"),
                set.getString("subject"),
                RssProvider.getProvider(set.getInt("provider")),
                set.getBoolean("nsfw")
        );
    }
}
