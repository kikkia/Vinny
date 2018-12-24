package com.bot.commands.reddit;

import com.bot.RedditConnection;
import com.bot.Utils.RedditHelper;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RandomPostCommand extends Command{
    private static final Logger LOGGER = Logger.getLogger(RandomPostCommand.class.getName());

    private RedditConnection redditConnection;

    public RandomPostCommand() {
        this.name = "rr";
        this.help = "Grabs a random hot post from a given subreddit";
        this.arguments = "<subreddit name>";

        redditConnection = RedditConnection.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        try{
            RedditHelper.getRandomSubmissionAndSend(redditConnection,
                    commandEvent,
                    SubredditSort.HOT,
                    TimePeriod.WEEK,
                    150);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error thrown:" + e);
            commandEvent.reply(commandEvent.getClient().getError() + " Sorry, something went wrong getting a reddit post.");
        }
    }
}
