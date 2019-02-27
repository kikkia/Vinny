package com.bot.commands.reddit;

import com.bot.RedditConnection;
import com.bot.db.ChannelDAO;
import com.bot.models.InternalTextChannel;
import com.bot.utils.CommandCategories;
import com.bot.utils.CommandPermissions;
import com.bot.utils.RedditHelper;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewPostCommand extends Command {
    private static final Logger LOGGER = Logger.getLogger(NewPostCommand.class.getName());

    private RedditConnection redditConnection;
    private ChannelDAO channelDAO;

    public NewPostCommand() {
        this.name = "nr";
        this.help = "Grabs a random new post from the newest 100 posts in a given subreddit";
        this.arguments = "<subreddit name>";
        this.category = CommandCategories.GENERAL;
        redditConnection = RedditConnection.getInstance();
        channelDAO = ChannelDAO.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        InternalTextChannel channel;
        try {
            channel = channelDAO.getTextChannelForId(commandEvent.getTextChannel().getId());
        } catch (SQLException e) {
            LOGGER.severe("Failed to get channel for random reddit command: " + e.getMessage());
            commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong getting the channel from the db. Please try again.");
            return;
        }

        try{
            RedditHelper.getRandomSubmissionAndSend(redditConnection,
                    commandEvent,
                    SubredditSort.NEW,
                    TimePeriod.ALL,
                    100,
                    channel.isNSFWEnabled());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error thrown:" + e);
            commandEvent.reply(commandEvent.getClient().getError() + " Sorry, something went wrong getting a reddit post.");
        }
    }
}
