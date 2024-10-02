package com.bot.db.models

import java.sql.ResultSet
import java.time.Instant

class OauthConfig(val userId: String, val refreshToken: String, val accessToken: String, val tokenType: String, val expiry: Instant) {

    // If expiry is within 10 mins, we should preemptive refresh
    fun needsRefresh() : Boolean {
        return expiry.isBefore(Instant.now().plusSeconds(600))
    }

    companion object {
        fun mapSetToOauthConfig(set: ResultSet) : OauthConfig {
            return OauthConfig(set.getString("id"),
                    set.getString("refresh_token"),
                    set.getString("access_token"),
                    set.getString("token_type"),
                    Instant.ofEpochSecond(set.getLong("expiry")))
        }
    }
}