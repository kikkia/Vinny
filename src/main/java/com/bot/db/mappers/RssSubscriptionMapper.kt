package com.bot.db.mappers

import com.bot.models.RssProvider.Companion.getProvider
import com.bot.models.RssSubscription
import java.sql.ResultSet
import java.sql.SQLException

object RssSubscriptionMapper {
    @JvmStatic
    @Throws(SQLException::class)
    fun mapSetToRssSubscription(set: ResultSet): RssSubscription {
        return RssSubscription(
                set.getInt("id"),
                set.getString("subject"),
                getProvider(set.getInt("provider")),
                set.getBoolean("nsfw")
        )
    }
}