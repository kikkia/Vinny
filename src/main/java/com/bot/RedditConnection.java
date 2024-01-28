package com.bot;

import com.bot.utils.VinnyConfig;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;

import java.util.UUID;

/**
 * Class that handles the generation and managment of the connection to the Reddit API
 */
public class RedditConnection {
    private static RedditConnection instance;
    private final RedditClient client;

    /**
     * Generates a new connection to reddit API.
     */
    private RedditConnection() {
        VinnyConfig config = VinnyConfig.Companion.instance();
        String clientID = config.getThirdPartyConfig().getRedditClientId();
        String redditSecret = config.getThirdPartyConfig().getRedditClientToken();
        // Load Credentials
        Credentials oauthCreds = Credentials.userless(clientID, redditSecret, UUID.randomUUID());

        // Create a unique InternalGuildMembership-Agent
        UserAgent userAgent = new UserAgent("bot", "kikkia.vinny", "1.0.0", "Kikkia");

        // Authenticate
        client = OAuthHelper.automatic(new OkHttpNetworkAdapter(userAgent), oauthCreds);
        client.setLogHttp(false);
    }

    public static RedditConnection getInstance() {
        if (instance == null)
            instance = new RedditConnection();
        return instance;
    }

    /**
     * Tries to reconnect to the reddit client. Called if the current connection is unhealthy.
     * @return a new RedditClient
     */
    public static RedditConnection refreshConnection() {
        instance = new RedditConnection();
        return instance;
    }

    /**
     * Determines if the current connection to the reddit API is healthy.
     * @return true if healty
     */
    public boolean isHealthy() {
        // TODO: Determine a way to judge health, probably just do a simple get request.
        return true;
    }

    public RedditClient getClient() {
        return client;
    }
}
