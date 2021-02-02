package com.bot.commands.rss;

import com.bot.models.RssProvider;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.SQLException;

public class SubscribeBdoCommand extends CreateSubscriptionCommand {

    public SubscribeBdoCommand(EventWaiter waiter) {
        this.name = "subscribebdo";
        this.botPermissions = new Permission[] {Permission.MANAGE_WEBHOOKS};
        this.waiter = waiter;
    }


    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        if (!canMakeNewSubscription(commandEvent)) {
            return;
        }
        TextChannel channel = commandEvent.getMessage().getMentionedChannels().size() > 0
                ? commandEvent.getMessage().getMentionedChannels().get(0) : commandEvent.getTextChannel();

        try {
            getRssDAO().addSubscription(RssProvider.BDO, "bdo", channel.getId(), commandEvent.getAuthor().getId(), false);
            commandEvent.replySuccess("Successfully subscribed to BDO news and patch notes.");
        } catch (SQLException e) {
            logger.severe("Error adding chan sub", e);
            commandEvent.replyError("Something went wrong adding the subscription, please try again.");
        }
    }
}
