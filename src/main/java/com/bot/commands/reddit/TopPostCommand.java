package com.bot.commands.reddit;

import com.bot.RedditConnection;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;


public class TopPostCommand extends Command{
    private RedditConnection redditConnection;

    public TopPostCommand() {
        redditConnection = RedditConnection.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // TODO: Get a random top post from a given subreddit (Limit: 100)
    }
}
