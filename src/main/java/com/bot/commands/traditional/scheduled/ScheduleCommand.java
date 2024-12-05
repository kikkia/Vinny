package com.bot.commands.traditional.scheduled;

import com.bot.commands.traditional.BaseCommand;
import com.bot.commands.traditional.ModerationCommand;
import com.bot.db.ScheduledCommandDAO;
import com.bot.db.UserDAO;
import com.bot.exceptions.IntervalFormatException;
import com.bot.models.InternalUser;
import com.bot.models.ScheduledCommand;
import com.bot.models.UsageLevel;
import com.bot.utils.AliasUtils;
import com.bot.utils.CommandCategories;
import com.bot.utils.ConstantStrings;
import com.bot.utils.FormattingUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ScheduleCommand extends ModerationCommand {

    private final EventWaiter waiter;
    private final ScheduledCommandDAO scheduledCommandDAO;
    private final UserDAO userDAO;

    public ScheduleCommand(EventWaiter waiter) {
        this.name = "schedule";
        this.aliases = new String[] {"schedulecommand", "setupschedule"};
        this.help = "Adds a scheduled command to the channel";
        this.botPermissions = new Permission[] {Permission.MANAGE_WEBHOOKS};
        this.guildOnly = true;
        this.waiter = waiter;
        this.scheduledCommandDAO = ScheduledCommandDAO.getInstance();
        this.userDAO = UserDAO.getInstance();
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "Schedule")
    protected void executeCommand(CommandEvent commandEvent) {
        // Shit code
        InternalUser user = null;
        try {
            user = userDAO.getById(commandEvent.getAuthor().getId());
        } catch (SQLException throwables) {
           logger.log(Level.WARNING, "Did not find user", throwables);
        }

        UsageLevel usageLevel = user == null ? UsageLevel.BASIC : user.usageLevel();

        if (scheduledCommandDAO.getCountOfScheduledForAuthor(commandEvent.getAuthor().getId()) >= usageLevel.getMaxScheduled()) {
            commandEvent.replyWarning("You can only make "+usageLevel.getMaxScheduled()+" scheduled commands. " +
                    "To be able to make more, you can subscribe on the Vinny support server." +
                    " To get a support server invite use `~support`.\nYou can also remove your current scheduled commands with the " +
                    "`~unschedule` command");
            return;
        }

        commandEvent.reply(ConstantStrings.SCHEDULED_COMMAND_SETUP_HELLO);

        waiter.waitForEvent(MessageReceivedEvent.class,
                e -> e.getAuthor().equals(commandEvent.getAuthor())
                        && e.getChannel().equals(commandEvent.getChannel())
                        && !e.getMessage().equals(commandEvent.getMessage()),
                new ScheduleCommand.StepOneConsumer(commandEvent),
                // if the user takes more than a minute, time out
                1, TimeUnit.MINUTES, () -> commandEvent.reply(ConstantStrings.EVENT_WAITER_TIMEOUT));
    }

    class StepOneConsumer implements Consumer<MessageReceivedEvent> {
        private String command;
        private final CommandEvent commandEvent;

        StepOneConsumer(CommandEvent commandEvent) {
            this.commandEvent = commandEvent;
        }

        @Override
        @Trace(operationName = "executeCommand", resourceName = "Schedule.stepOne")
        public void accept(MessageReceivedEvent event) {
            if (event.getMessage().getContentRaw().equals("?")) {
                commandEvent.reply(ConstantStrings.SCHEDULED_COMMANDS_HELP);
                return;
            }

            BaseCommand commandToInvoke = (BaseCommand) AliasUtils.findCommandForInput(event.getMessage().getContentRaw().split(" ")[0]);
            command = "~" + event.getMessage().getContentRaw();

            if (commandToInvoke == null) {
                commandEvent.replyWarning("That does not seem like it would trigger any commands. Please try again.");
            } else if (commandToInvoke.getCategory() == CommandCategories.VOICE) {
                commandEvent.replyWarning("Voice commands cannot be scheduled.");
            } else if (commandToInvoke.getCategory() == CommandCategories.MODERATION) {
                commandEvent.replyWarning("Moderation commands cannot be scheduled.");
            } else if (!commandToInvoke.canSchedule) {
                commandEvent.replyWarning("This command cannot be scheduled.");
            } else {
                commandEvent.replySuccess(ConstantStrings.SCHEDULED_COMMAND_SETUP_INTERVAL);

                waiter.waitForEvent(MessageReceivedEvent.class,
                        e -> e.getAuthor().equals(event.getAuthor())
                                && e.getChannel().equals(commandEvent.getChannel()),
                        new StepTwoConsumer(commandEvent, command),
                        1, TimeUnit.MINUTES, () -> commandEvent.replyWarning(ConstantStrings.EVENT_WAITER_TIMEOUT));
            }
        }
    }

    class StepTwoConsumer implements Consumer<MessageReceivedEvent> {
        private final String command;
        private final CommandEvent commandEvent;

        StepTwoConsumer(CommandEvent commandEvent, String command) {
            this.command = command;
            this.commandEvent = commandEvent;
        }

        @Override
        @Trace(operationName = "executeCommand", resourceName = "Schedule.stepTwo")
        public void accept(MessageReceivedEvent event) {
            long interval;
            try {
                interval = FormattingUtils.getTimeForScheduledInput(event.getMessage().getContentRaw());
            } catch (IntervalFormatException e) {
                commandEvent.replyWarning("Failed to parse interval. `" + e.getMessage() + "`");
                return;
            }

            ScheduledCommand scheduledCommand = new ScheduledCommand(0,
                    command,
                    event.getGuild().getId(),
                    event.getChannel().asTextChannel().getId(),
                    commandEvent.getAuthor().getId(),
                    interval,
                    System.currentTimeMillis());

            try {
                scheduledCommandDAO.addScheduledCommand(scheduledCommand);
                commandEvent.replySuccess(ConstantStrings.SCHEDULED_COMMAND_SETUP_COMPLETE +
                        FormattingUtils.getDurationBreakdown(interval));
            } catch (SQLException e) {
                logger.severe("Failed to add scheduled command", e);
                commandEvent.replyError("Something went wrong when scheduling the command.");
            }
        }
    }
}
