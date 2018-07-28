package com.bot;

import net.dean.jraw.RedditClient;

/**
 * Class that handles the generation and managment of the connection to the Reddit API
 */
public class RedditConnection {
    private static RedditConnection instance;
    private RedditClient client;

    /**
     * Generates a new connection to reddit API.
     */
    private RedditConnection() {
        // TODO: Generate new client to connect to reddit
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
