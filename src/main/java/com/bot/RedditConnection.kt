package com.bot

import com.bot.utils.VinnyConfig.Companion.instance
import net.dean.jraw.RedditClient
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper.automatic
import java.util.*

/**
 * Class that handles the generation and managment of the connection to the Reddit API
 */
class RedditConnection private constructor() {
    @JvmField
    val client: RedditClient

    /**
     * Generates a new connection to reddit API.
     */
    init {
        val config = instance()
        val clientID = config.thirdPartyConfig!!.redditClientId
        val redditSecret = config.thirdPartyConfig.redditClientToken
        // Load Credentials
        val oauthCreds = Credentials.userless(clientID!!, redditSecret!!, UUID.randomUUID())

        // Create a unique InternalGuildMembership-Agent
        val userAgent = UserAgent("bot", "kikkia.vinny", "1.0.0", "Kikkia")

        // Authenticate
        client = automatic(OkHttpNetworkAdapter(userAgent), oauthCreds)
        client.logHttp = false
    }

    val isHealthy: Boolean
        /**
         * Determines if the current connection to the reddit API is healthy.
         * @return true if healty
         */
        get() =// TODO: Determine a way to judge health, probably just do a simple get request.
            true

    companion object {
        private var instance: RedditConnection? = null
        @JvmStatic
        fun getInstance(): RedditConnection {
            if (instance == null) instance = RedditConnection()
            return instance!!
        }

        /**
         * Tries to reconnect to the reddit client. Called if the current connection is unhealthy.
         * @return a new RedditClient
         */
        fun refreshConnection(): RedditConnection {
            instance = RedditConnection()
            return instance!!
        }
    }
}
