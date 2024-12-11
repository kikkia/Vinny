package com.bot.commands.traditional.rss;

import com.bot.exceptions.BlueskyException;
import com.bot.models.RssProvider;
import com.bot.utils.ConstantStrings;
import com.bot.utils.HttpUtils;
import com.bot.utils.RssUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SubscribeBlueskyCommand extends CreateSubscriptionCommand {

    public SubscribeBlueskyCommand(EventWaiter waiter) {
        this.name = "subscribebluesky";
        this.aliases = new String[] {"subbs", "bluesky"};
        this.botPermissions = new Permission[] {Permission.MANAGE_WEBHOOKS};
        this.waiter = waiter;
    }

    @Override
    //@Trace(operationName = "executeCommand", resourceName = "SubscribeTwitter")
    public void executeCommand(CommandEvent commandEvent) {
        if (!canMakeNewSubscription(commandEvent)) {
            return;
        }

        commandEvent.reply(ConstantStrings.BLUESKY_SUB_HELLO);

        waiter.waitForEvent(MessageReceivedEvent.class,
                e -> e.getAuthor().equals(commandEvent.getAuthor())
                        && e.getChannel().equals(commandEvent.getChannel())
                        && !e.getMessage().equals(commandEvent.getMessage()),
                new SubscribeBlueskyCommand.StepOneConsumer(commandEvent),
                // if the user takes more than a minute, time out
                1, TimeUnit.MINUTES, () -> commandEvent.reply(ConstantStrings.EVENT_WAITER_TIMEOUT));
    }

    class StepOneConsumer implements Consumer<MessageReceivedEvent> {
        private final CommandEvent commandEvent;

        StepOneConsumer(CommandEvent commandEvent) {
            this.commandEvent = commandEvent;
        }

        @Override
        //@Trace(operationName = "executeCommand", resourceName = "SubscribeTwitter.stepOne")
        public void accept(MessageReceivedEvent event) {
            String subject = event.getMessage().getContentRaw().replaceAll("@", "");

            if (!RssUtils.isBlueSkyHandleValid(subject)) {
                commandEvent.replyWarning(ConstantStrings.BLUESKY_HANDLE_NOT_VALID);
                return;
            } else if (!event.getChannel().asTextChannel().isNSFW()) {
                commandEvent.replyWarning(ConstantStrings.BLUESKY_NSFW);
                return;
            }

            try {
                String rssUrl = HttpUtils.getBlueSkyRSS(subject);

                getRssDAO().addSubscription(RssProvider.BLUESKY, cleanUrl(rssUrl), event.getChannel().getId(), event.getAuthor().getId(), true, subject);
            } catch (SQLException e) {
                logger.severe("Error adding bluesky sub", e);
                commandEvent.replyError("Something went wrong adding the subscription, please try again.");
                return;
            } catch (BlueskyException e) {
                commandEvent.replyError(e.getMessage());
                return;
            }
            commandEvent.replySuccess(ConstantStrings.BLUESKY_SUB_SUCCESS);
        }
    }

    public String cleanUrl(String url) {
        if (url == null || !url.contains("/profile/") || !url.contains("/rss")) {
            throw new BlueskyException("Failed to parse bluesky RSS url. Please let me know on the support server.");
        }

        String profilePart = "/profile/";
        String rssPart = "/rss";

        int startIndex = url.indexOf(profilePart) + profilePart.length();
        int endIndex = url.indexOf(rssPart);

        return url.substring(startIndex, endIndex);
    }
}
