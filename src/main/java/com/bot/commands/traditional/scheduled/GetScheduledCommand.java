package com.bot.commands.traditional.scheduled;

import com.bot.commands.traditional.GeneralCommand;
import com.bot.db.ScheduledCommandDAO;
import com.bot.models.ScheduledCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import datadog.trace.api.Trace;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GetScheduledCommand extends GeneralCommand {

    private final ScheduledCommandDAO scheduledCommandDAO;
    private final Paginator.Builder builder;

    public GetScheduledCommand(EventWaiter waiter) {
        this.name = "scheduled";
        this.help = "Shows all scheduled commands in the guild, channel or for you";
        this.arguments = "<{g} or {c} or {me}";
        this.guildOnly = true;
        this.scheduledCommandDAO = ScheduledCommandDAO.getInstance();

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
    @Trace(operationName = "executeCommand", resourceName = "GetScheduled")
    protected void executeCommand(CommandEvent commandEvent) {
        String args = commandEvent.getArgs();
        if (!(args.equalsIgnoreCase("g") ||
                args.equalsIgnoreCase("c") ||
                args.equalsIgnoreCase("me"))) {
            commandEvent.replyWarning("Invalid argument, please give a `g` for guild, `c` for " +
                    "channel or `me` for your own.");
            return;
        }

        List<ScheduledCommand> commandList = new ArrayList<>();
        try {
            if (args.equalsIgnoreCase("g"))
                commandList = scheduledCommandDAO.getAllScheduledCommandsForGuild(commandEvent.getGuild().getId());
            else if (args.equalsIgnoreCase("c"))
                commandList = scheduledCommandDAO.getAllScheduledCommandsForChannel(commandEvent.getChannel().getId());
            else if (args.equalsIgnoreCase("me"))
                commandList = scheduledCommandDAO.getAllScheduledCommandsForAuthor(commandEvent.getAuthor().getId());
        } catch (SQLException e) {
            logger.severe("Failed to get scheduled commands", e);
            commandEvent.replyError("Something went wrong getting the scheduled commands");
            return;
        }

        if (commandList.isEmpty()) {
            commandEvent.replyWarning("I could not find any scheduled commands.");
            return;
        }

        List<String> strings = commandList.stream().map(ScheduledCommand::toString).collect(Collectors.toList());
        builder.setItems(strings.toArray(new String[]{}));
        builder.setText("Scheduled commands in context");

        builder.build().paginate(commandEvent.getChannel(), 1);
    }
}
