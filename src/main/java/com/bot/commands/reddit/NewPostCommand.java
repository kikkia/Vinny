package com.bot.commands.reddit;

import com.bot.RedditConnection;
import com.bot.commands.RedditCommand;
import com.bot.exceptions.RedditRateLimitException;
import com.bot.exceptions.ScheduledCommandFailedException;
import com.bot.utils.CommandCategories;
import com.bot.utils.ConstantStrings;
import com.bot.utils.RedditHelper;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dv8tion.jda.api.entities.ChannelType;

import java.util.logging.Level;

public class NewPostCommand extends RedditCommand {
    private final RedditConnection redditConnection;

    public NewPostCommand() {
        this.name = "nr";
        this.help = "Grabs a random new post from the newest 100 posts in a given subreddit";
        this.category = CommandCategories.REDDIT;
        redditConnection = RedditConnection.getInstance();
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "NewReddit")
    protected void executeCommand(CommandEvent commandEvent) {
        boolean isNSFWAllowed = true;
        boolean postOnly = commandEvent.getArgs().contains("--post-only");
        String args = parseArgs(commandEvent.getArgs());

        // TODO: Move to static helper
        if (!commandEvent.isFromType(ChannelType.PRIVATE)) {
            isNSFWAllowed = commandEvent.getTextChannel().isNSFW();
        }

        try{
            RedditHelper.getRandomSubmissionAndSend(redditConnection,
                    commandEvent,
                    SubredditSort.NEW,
                    TimePeriod.ALL,
                    100,
                    isNSFWAllowed,
                    postOnly,
                    args);
        } catch (NullPointerException e) {
            commandEvent.replyWarning("I could not find that subreddit, please make sure it is public, and spelled correctly.");
        } catch (ApiException e) {
            if (e.getCode().equals("403")) {
                commandEvent.replyWarning("Subreddit is private, please stick to public subreddits.");
            } else {
                commandEvent.replyError("Recieved error: " + e.getCode() + " from reddit.");
            }
        } catch (NetworkException e) {
            commandEvent.replyWarning("I was unable to get info the subreddit, please make sure it is correctly spelled.");
        } catch (ScheduledCommandFailedException e) {
            logger.warning("Failed to get webhook for scheduled command " + commandEvent.getTextChannel().getId(), e);
            commandEvent.replyWarning(ConstantStrings.SCHEDULED_WEBHOOK_FAIL);
        } catch (RedditRateLimitException e) {
            commandEvent.reply(commandEvent.getClient().getError() + "Reddit is rate limiting Vinny, please try again later.");
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error thrown: " + e);
            commandEvent.replyError("Sorry, something went wrong getting a reddit post.");
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
        }
    }
}
