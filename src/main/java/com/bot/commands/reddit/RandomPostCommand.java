package com.bot.commands.reddit;

import com.bot.RedditConnection;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

public class RandomPostCommand extends Command{
    private RedditConnection redditConnection;

    public RandomPostCommand() {
        redditConnection = RedditConnection.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // TODO: Get a random hot post from a given subreddit. (Limit: 100)
    }
}
