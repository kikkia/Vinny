package com.bot.commands.rss;

import com.bot.RedditConnection;
import com.bot.models.RssProvider;
import com.bot.utils.ConstantStrings;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import datadog.trace.api.Trace;
import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Subreddit;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SubscribeRedditCommand extends CreateSubscriptionCommand {
    private RedditClient redditClient;

    public SubscribeRedditCommand(EventWaiter waiter) {
        this.name = "subscribereddit";
        this.aliases = new String[] {"redditsubscribe", "subscribesubreddit"};
        this.botPermissions = new Permission[] {Permission.MANAGE_WEBHOOKS};
        this.waiter = waiter;
        this.redditClient = RedditConnection.getInstance().getClient();
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "SubscribeReddit")
    protected void executeCommand(CommandEvent commandEvent) {
        if (!canMakeNewSubscription(commandEvent)) {
            return;
        }

        commandEvent.reply(ConstantStrings.SUBREDDIT_SUB_HELLO);

        waiter.waitForEvent(MessageReceivedEvent.class,
                e -> e.getAuthor().equals(commandEvent.getAuthor())
                        && e.getChannel().equals(commandEvent.getChannel())
                        && !e.getMessage().equals(commandEvent.getMessage()),
                new SubscribeRedditCommand.StepOneConsumer(commandEvent),
                // if the user takes more than a minute, time out
                1, TimeUnit.MINUTES, () -> commandEvent.reply(ConstantStrings.EVENT_WAITER_TIMEOUT));
    }

    class StepOneConsumer implements Consumer<MessageReceivedEvent> {
        private CommandEvent commandEvent;

        StepOneConsumer(CommandEvent commandEvent) {
            this.commandEvent = commandEvent;
        }

        @Override
        @Trace(operationName = "executeCommand", resourceName = "SubscribeReddit.stepOne")
        public void accept(MessageReceivedEvent event) {
            String subject = event.getMessage().getContentRaw();
            Subreddit subreddit = redditClient.subreddit(subject).about();
            if (subreddit == null) {
                commandEvent.replyWarning(ConstantStrings.SUBREDDIT_INVALID);
                return;
            } else if (subreddit.isNsfw() && !event.getTextChannel().isNSFW()) {
                commandEvent.replyWarning(ConstantStrings.SUBREDDIT_NSFW);
                return;
            }

            // TODO: Keyword handler
            try {
                getRssDAO().addSubscription(RssProvider.REDDIT, subject, event.getChannel().getId(), event.getAuthor().getId(), subreddit.isNsfw());
            } catch (SQLException e) {
                logger.severe("Error adding twitter sub", e);
                commandEvent.replyError("Something went wrong adding the subscription, please try again.");
            }
            commandEvent.replySuccess(ConstantStrings.SUBREDDIT_SUBSCRIBE_SUCCESS);
        }
    }
}
