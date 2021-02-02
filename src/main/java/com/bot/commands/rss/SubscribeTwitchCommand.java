package com.bot.commands.rss;

import com.bot.exceptions.NoSuchResourceException;
import com.bot.models.RssProvider;
import com.bot.utils.ConstantStrings;
import com.bot.utils.HttpUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SubscribeTwitchCommand extends CreateSubscriptionCommand {

    public SubscribeTwitchCommand(EventWaiter waiter) {
        this.name = "subscribetwitch";
        this.botPermissions = new Permission[] {Permission.MANAGE_WEBHOOKS};
        this.waiter = waiter;
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "SubscribeTwitch")
    protected void executeCommand(CommandEvent commandEvent) {
        if (!canMakeNewSubscription(commandEvent)) {
            return;
        }

        commandEvent.reply(ConstantStrings.TWITCH_SUB_HELLO);

        waiter.waitForEvent(MessageReceivedEvent.class,
                e -> e.getAuthor().equals(commandEvent.getAuthor())
                        && e.getChannel().equals(commandEvent.getChannel())
                        && !e.getMessage().equals(commandEvent.getMessage()),
                new SubscribeTwitchCommand.StepOneConsumer(commandEvent),
                // if the user takes more than a minute, time out
                1, TimeUnit.MINUTES, () -> commandEvent.reply(ConstantStrings.EVENT_WAITER_TIMEOUT));
    }

    class StepOneConsumer implements Consumer<MessageReceivedEvent> {
        private CommandEvent commandEvent;

        StepOneConsumer(CommandEvent commandEvent) {
            this.commandEvent = commandEvent;
        }

        @Override
        @Trace(operationName = "executeCommand", resourceName = "SubscribeTwitch.stepOne")
        public void accept(MessageReceivedEvent event) {
            String subject = event.getMessage().getContentRaw();
            String id;
            if (subject.toLowerCase().contains("https://www.twitch.tv/")
                    || subject.toLowerCase().contains("https://twitch.tv/")) {
                subject = subject.split("/")[subject.split("/").length - 1];
            }

            try {
                id = HttpUtils.getTwitchIdForUsername(subject);
            }  catch (NoSuchResourceException e) {
                commandEvent.replyWarning(ConstantStrings.TWITCH_SUB_NOT_FOUND);
                return;
            } catch (Exception e) {
                logger.severe("Failed to get user from twitch", e);
                commandEvent.replyError("Failed to get user from twitch, please try again.");
                return;
            }

            try {
                getRssDAO().addSubscription(RssProvider.TWITCH, id, event.getChannel().getId(), event.getAuthor().getId(), false);
            } catch (SQLException e) {
                logger.severe("Error adding twitch sub", e);
                commandEvent.replyError("Something went wrong adding the subscription, please try again.");
            }
            commandEvent.replySuccess(ConstantStrings.TWITCH_SUB_SUCCESS);
        }
    }
}
