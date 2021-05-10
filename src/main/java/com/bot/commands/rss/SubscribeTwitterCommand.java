package com.bot.commands.rss;

import com.bot.models.RssProvider;
import com.bot.utils.ConstantStrings;
import com.bot.utils.RssUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SubscribeTwitterCommand extends CreateSubscriptionCommand {

    public SubscribeTwitterCommand(EventWaiter waiter) {
        this.name = "subscribetwitter";
        this.botPermissions = new Permission[] {Permission.MANAGE_WEBHOOKS};
        this.waiter = waiter;
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "SubscribeTwitter")
    public void executeCommand(CommandEvent commandEvent) {
        if (!canMakeNewSubscription(commandEvent)) {
            return;
        }

        commandEvent.reply(ConstantStrings.TWITTER_SUB_HELLO);

        waiter.waitForEvent(MessageReceivedEvent.class,
                e -> e.getAuthor().equals(commandEvent.getAuthor())
                        && e.getChannel().equals(commandEvent.getChannel())
                        && !e.getMessage().equals(commandEvent.getMessage()),
                new SubscribeTwitterCommand.StepOneConsumer(commandEvent),
                // if the user takes more than a minute, time out
                1, TimeUnit.MINUTES, () -> commandEvent.reply(ConstantStrings.EVENT_WAITER_TIMEOUT));
    }

    class StepOneConsumer implements Consumer<MessageReceivedEvent> {
        private CommandEvent commandEvent;

        StepOneConsumer(CommandEvent commandEvent) {
            this.commandEvent = commandEvent;
        }

        @Override
        @Trace(operationName = "executeCommand", resourceName = "SubscribeTwitter.stepOne")
        public void accept(MessageReceivedEvent event) {
            String subject = event.getMessage().getContentRaw();
            subject = subject.replaceAll("@", "");
            // TODO: Check username is valid
            if (!RssUtils.isTwitterHandleValid(subject)) {
                commandEvent.replyWarning(ConstantStrings.TWITTER_HANDLE_NOT_VALID);
                return;
            } else if (!event.getTextChannel().isNSFW()) {
                commandEvent.replyWarning(ConstantStrings.TWITTER_NSFW);
                return;
            }

            try {
                getRssDAO().addSubscription(RssProvider.TWITTER, subject, event.getChannel().getId(), event.getAuthor().getId(), true);
            } catch (SQLException e) {
                logger.severe("Error adding twitter sub", e);
                commandEvent.replyError("Something went wrong adding the subscription, please try again.");
            }
            commandEvent.replySuccess(ConstantStrings.TWITTER_SUB_SUCCESS);
        }
    }
}
