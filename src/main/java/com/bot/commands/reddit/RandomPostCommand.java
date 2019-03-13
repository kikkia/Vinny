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

public class RandomPostCommand extends RedditCommand{
    private static final Logger LOGGER = Logger.getLogger(RandomPostCommand.class.getName());

    private RedditConnection redditConnection;
    private ChannelDAO channelDAO;

    public RandomPostCommand() {
        this.name = "rr";
        this.help = "Grabs a random hot post from a given subreddit";
        this.arguments = "<subreddit name>";
        this.category = CommandCategories.REDDIT;

        redditConnection = RedditConnection.getInstance();
        channelDAO = ChannelDAO.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        boolean isNSFWAllowed = true;

        // TODO: Move to static helper
        if (!commandEvent.isFromType(ChannelType.PRIVATE)) {
            InternalTextChannel channel = channelDAO.getTextChannelForId(commandEvent.getTextChannel().getId());

            if (channel == null) {
                commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong getting the channel from the db. Please try again.");
                metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
                return;
            }

            isNSFWAllowed = channel.isNSFWEnabled();
        }

        try{
            RedditHelper.getRandomSubmissionAndSend(redditConnection,
                    commandEvent,
                    SubredditSort.HOT,
                    TimePeriod.WEEK,
                    150,
                    isNSFWAllowed);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error thrown:" + e);
            commandEvent.reply(commandEvent.getClient().getError() + " Sorry, something went wrong getting a reddit post.");
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
        }
    }
}
