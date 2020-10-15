package com.bot.commands.rss;

import com.bot.commands.GeneralCommand;
import com.bot.db.RssDAO;
import com.bot.models.RssChannelSubscription;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import datadog.trace.api.Trace;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SubscriptionsCommand extends GeneralCommand {

    private RssDAO rssDAO;
    private final Paginator.Builder builder;

    public SubscriptionsCommand(EventWaiter waiter) {
        this.name = "subscriptions";
        this.help = "Shows all subscriptions for the channel or for you";
        this.arguments = "<{c} or {me}>";
        this.guildOnly = true;
        this.rssDAO = RssDAO.getInstance();

        this.builder = new Paginator.Builder()
                .setColumns(1)
                .setItemsPerPage(1)
                .useNumberedItems(false)
                .showPageNumbers(true)
                .showPageNumbers(true)
                .setEventWaiter(waiter)
                .setTimeout(30, TimeUnit.SECONDS)
                .waitOnSinglePage(false)
                .setFinalAction(message -> message.clearReactions().queue());
    }

    @Override
    //@trace(operationName = "executeCommand", resourceName = "Subscriptions")
    protected void executeCommand(CommandEvent commandEvent) {
        String args = commandEvent.getArgs();
        if (!(args.equalsIgnoreCase("c") ||
                args.equalsIgnoreCase("me"))) {
            commandEvent.replyWarning("Invalid argument, please specify `c` for " +
                    "channel or `me` for your own.");
            return;
        }

        List<RssChannelSubscription> subscriptions = new ArrayList<>();
        try {
            if (args.equalsIgnoreCase("c"))
                subscriptions = rssDAO.getSubscriptionsForChannel(commandEvent.getChannel().getId());
            else if (args.equalsIgnoreCase("me"))
                subscriptions = rssDAO.getSubscriptionsForAuthor(commandEvent.getAuthor().getId());
        } catch (SQLException e) {
            logger.severe("Failed to get subscriptions", e);
            commandEvent.replyError("Something went wrong getting the subscriptions");
            return;
        }

        if (subscriptions.isEmpty()) {
            commandEvent.replyWarning("I could not find any subscriptions.");
            return;
        }

        List<String> strings = subscriptions.stream().map(RssChannelSubscription::toString).collect(Collectors.toList());
        builder.setItems(strings.toArray(new String[]{}));
        builder.setText("Subscriptions in context");

        builder.build().paginate(commandEvent.getTextChannel(), 1);
    }
}

