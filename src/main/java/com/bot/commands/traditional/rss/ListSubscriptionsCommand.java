package com.bot.commands.traditional.rss;

import com.bot.commands.traditional.GeneralCommand;
import com.bot.db.RssDAO;
import com.bot.models.RssChannelSubscription;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ListSubscriptionsCommand extends GeneralCommand {

    private final RssDAO rssDAO;
    private final Paginator.Builder builder;

    public ListSubscriptionsCommand(EventWaiter waiter) {
        this.name = "listsubs";
        this.help = "Lists basic info for subs in the channel";
        this.guildOnly = true;
        this.rssDAO = RssDAO.getInstance();

        this.builder = new Paginator.Builder()
                .setColumns(1)
                .setItemsPerPage(10)
                .useNumberedItems(false)
                .showPageNumbers(true)
                .showPageNumbers(true)
                .setEventWaiter(waiter)
                .setTimeout(30, TimeUnit.SECONDS)
                .waitOnSinglePage(false)
                .setFinalAction(message -> message.clearReactions().queue());
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        List<RssChannelSubscription> subscriptions;
        try {
            subscriptions = rssDAO.getSubscriptionsForChannel(commandEvent.getChannel().getId());
        } catch (SQLException throwables) {
            logger.severe("Failed to get subs", throwables);
            throwables.printStackTrace();
            commandEvent.replyError("Something went wrong getting subs, please try again later.");
            return;
        }

        if (subscriptions.isEmpty()) {
            commandEvent.replyWarning("No subscriptions in this channel.");
            return;
        }

        List<String> subs = subscriptions.stream().map(RssChannelSubscription::toCondensedString).toList();
        builder.setItems(subs.toArray(new String[]{}));
        builder.setText("Subscriptions in channel");

        builder.build().paginate(commandEvent.getChannel(), 1);
    }
}
