package com.bot.commands.reddit;

import com.bot.RedditConnection;
import com.bot.utils.CommandCategories;
import com.bot.utils.CommandPermissions;
import com.bot.utils.RedditHelper;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NewPostCommand extends Command {
    private static final Logger LOGGER = Logger.getLogger(NewPostCommand.class.getName());

    private RedditConnection redditConnection;

    public NewPostCommand() {
        this.name = "nr";
        this.help = "Grabs a random new post from the newest 100 posts in a given subreddit";
        this.arguments = "<subreddit name>";
        this.category = CommandCategories.GENERAL;
        redditConnection = RedditConnection.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        // TODO: NSFW Checks as well
        try{
            RedditHelper.getRandomSubmissionAndSend(redditConnection,
                    commandEvent,
                    SubredditSort.NEW,
                    TimePeriod.ALL,
                    100);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error thrown:" + e);
            commandEvent.reply(commandEvent.getClient().getError() + " Sorry, something went wrong getting a reddit post.");
        }
    }
}
