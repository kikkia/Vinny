package com.bot.commands.rss;

import com.bot.exceptions.InvalidInputException;
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

public class SubscribeYoutubeCommand extends CreateSubscriptionCommand {

    public SubscribeYoutubeCommand(EventWaiter waiter) {
        this.name = "subscribeyt";
        this.aliases = new String[] {"subscribeyoutube"};
        this.botPermissions = new Permission[] {Permission.MANAGE_WEBHOOKS};
        this.waiter = waiter;
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "SubscribeYoutube")
    protected void executeCommand(CommandEvent commandEvent) {
        if (!canMakeNewSubscription(commandEvent)) {
            return;
        }

        commandEvent.reply(ConstantStrings.YT_SUB_HELLO);
        waiter.waitForEvent(MessageReceivedEvent.class,
                e -> e.getAuthor().equals(commandEvent.getAuthor())
                        && e.getChannel().equals(commandEvent.getChannel())
                        && !e.getMessage().equals(commandEvent.getMessage()),
                new SubscribeYoutubeCommand.StepOneConsumer(commandEvent),
                // if the user takes more than a minute, time out
                1, TimeUnit.MINUTES, () -> commandEvent.reply(ConstantStrings.EVENT_WAITER_TIMEOUT));
    }
    class StepOneConsumer implements Consumer<MessageReceivedEvent> {
        private CommandEvent commandEvent;

        StepOneConsumer(CommandEvent commandEvent) {
            this.commandEvent = commandEvent;
        }

        @Override
        @Trace(operationName = "executeCommand", resourceName = "SubscribeYoutube.stepOne")
        public void accept(MessageReceivedEvent event) {
            String channelUrl = event.getMessage().getContentRaw();
            try {
                String id = HttpUtils.getYoutubeIdForChannelUrl(channelUrl);
                getRssDAO().addSubscription(RssProvider.YOUTUBE, id, event.getChannel().getId(), event.getAuthor().getId(), false);
            }  catch (NoSuchResourceException e) {
                commandEvent.replyWarning(ConstantStrings.YT_SUB_NOT_FOUND);
                return;
            } catch (InvalidInputException e) {
                commandEvent.replyWarning("That link does not look right, make sure it is a link to their youtube.com" +
                        " channel page.");
                return;
            } catch (SQLException e) {
                logger.severe("Error adding youtube sub", e);
                commandEvent.replyError("Something went wrong adding the subscription, please try again.");
                return;
            } catch (Exception e) {
                logger.severe("Failed to get user from Youtube", e);
                commandEvent.replyError("Failed to get user from Youtube, please make sure it is a direct link to their" +
                        " channel. Please try again.");
                return;
            }

            commandEvent.replySuccess(ConstantStrings.YT_SUB_SUCCESS);
        }
    }
}
