package com.bot.commands.rss;

import com.bot.commands.ModerationCommand;
import com.bot.db.ChannelDAO;
import com.bot.db.RssDAO;
import com.bot.models.InternalChannel;
import com.bot.models.RssChannelSubscription;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class RemoveSubscriptionCommand extends ModerationCommand {

    private RssDAO rssDAO;
    private ChannelDAO channelDAO;

    public RemoveSubscriptionCommand(RssDAO rssDAO, ChannelDAO channelDAO) {
        this.name = "unsubscribe";
        this.aliases = new String[] {"removesub", "removesubscription"};
        this.rssDAO = rssDAO;
        this.channelDAO = channelDAO;
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "RemoveSubscription")
    protected void executeCommand(CommandEvent commandEvent) {
        if (commandEvent.getArgs().isEmpty()) {
            commandEvent.replyWarning("Please give the id of the subscription to end. You can find this ID using the `~subscriptions` command.");
        }
        else {
            int id;
            try {
                id = Integer.parseInt(commandEvent.getArgs());
            } catch (Exception e) {
                commandEvent.replyWarning("Please provide a valid id (Integer)");
                return;
            }

            RssChannelSubscription subscription;
            InternalChannel channel;
            try {
                subscription = rssDAO.getChannelSubById(id);
                channel = channelDAO.getTextChannelForId(subscription.getChannel());
            } catch (SQLException e) {
                commandEvent.replyError("Failed to remove subscription");
                logger.severe("Failed to get sub or channel from db", e);
                return;
            }
            if (subscription == null) {
                commandEvent.replyWarning("Failed to find subscription for ID");
                return;
            }

            if (!channel.getGuildId().equals(commandEvent.getGuild().getId())) {
                commandEvent.replyWarning("You cannot remove this subscription unless you are in the guild it is in.");
                return;
            }

            try {
                rssDAO.removeChannelSubscription(subscription);
                commandEvent.reactSuccess();
            } catch (SQLException e) {
                logger.severe("Failed to remove channel sub", e);
                commandEvent.replyError("Failed to remove the subscription");
            }
        }
    }
}
