package com.bot.commands.reddit;

import com.bot.commands.RedditCommand;
import com.bot.RedditConnection;
import com.bot.db.ChannelDAO;
import com.bot.models.InternalTextChannel;
import com.bot.utils.CommandCategories;
import com.bot.utils.CommandPermissions;
import com.bot.utils.RedditHelper;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dv8tion.jda.core.entities.ChannelType;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NewPostCommand extends RedditCommand {
    private static final Logger LOGGER = Logger.getLogger(NewPostCommand.class.getName());

    private RedditConnection redditConnection;
    private ChannelDAO channelDAO;

    public NewPostCommand() {
        this.name = "nr";
        this.help = "Grabs a random new post from the newest 100 posts in a given subreddit";
        this.arguments = "<subreddit name>";
        this.category = CommandCategories.REDDIT;
        redditConnection = RedditConnection.getInstance();
        channelDAO = ChannelDAO.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        boolean isNSFWAllowed = true;

        if (!commandEvent.isFromType(ChannelType.PRIVATE)) {
            InternalTextChannel channel = channelDAO.getTextChannelForId(commandEvent.getTextChannel().getId());

            if (channel == null) {
                commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong getting the channel from the db. Please try again.");
                return;
            }

            isNSFWAllowed = channel.isNSFWEnabled();
        }

        try{
            RedditHelper.getRandomSubmissionAndSend(redditConnection,
                    commandEvent,
                    SubredditSort.NEW,
                    TimePeriod.ALL,
                    100,
                    isNSFWAllowed);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error thrown:" + e);
            commandEvent.reply(commandEvent.getClient().getError() + " Sorry, something went wrong getting a reddit post.");
        }
    }
}
