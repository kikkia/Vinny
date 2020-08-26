package com.bot.db.mappers

import com.bot.models.RssChannelSubscription
import com.bot.models.RssSubscription
import java.sql.ResultSet
import java.sql.SQLException

object RssChannelSubscriptionMapper {
    @JvmStatic
    @Throws(SQLException::class)
    fun mapToRssSubscription(set: ResultSet, subscription: RssSubscription?): RssChannelSubscription {
        return RssChannelSubscription(
                set.getInt("id"),
                subscription!!,
                set.getString("text_channel_id"),
                set.getString("author")
        )
    }
}