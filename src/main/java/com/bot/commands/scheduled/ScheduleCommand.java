package com.bot.commands.scheduled;

import com.bot.commands.ModerationCommand;
import com.bot.db.ScheduledCommandDAO;
import com.bot.exceptions.IntervalFormatException;
import com.bot.models.ScheduledCommand;
import com.bot.utils.AliasUtils;
import com.bot.utils.CommandCategories;
import com.bot.utils.ConstantStrings;
import com.bot.utils.FormattingUtils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ScheduleCommand extends ModerationCommand {

    private EventWaiter waiter;
    private ScheduledCommandDAO scheduledCommandDAO;

    public ScheduleCommand(EventWaiter waiter) {
        this.name = "schedule";
        this.aliases = new String[] {"schedulecommand", "setupschedule"};
        this.help = "Adds a scheduled command to the channel";
        this.guildOnly = true;
        this.waiter = waiter;
        this.scheduledCommandDAO = ScheduledCommandDAO.getInstance();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
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
        private CommandEvent commandEvent;

        public StepOneConsumer(CommandEvent commandEvent) {
            this.commandEvent = commandEvent;
        }

        @Override
        public void accept(MessageReceivedEvent event) {
            Command commandToInvoke = AliasUtils.findCommandForInput(event.getMessage().getContentRaw().split(" ")[0]);
            command = "~" + event.getMessage().getContentRaw();

            if (commandToInvoke == null) {
                commandEvent.replyWarning("That does not seem like it would trigger any commands. Please try again.");
            } else if (commandToInvoke.getCategory() == CommandCategories.VOICE) {
                commandEvent.replyWarning("Voice commands cannot be scheduled");
            } else if (commandToInvoke.getCategory() == CommandCategories.MODERATION) {
                commandEvent.replyWarning("Moderation commands cannot be scheduled");
            } else {
                commandEvent.replySuccess(ConstantStrings.SCHEDULED_COMMAND_SETUP_INTERVAL);

                waiter.waitForEvent(MessageReceivedEvent.class,
                        e -> e.getAuthor().equals(event.getAuthor()),
                        new StepTwoConsumer(commandEvent, command),
                        1, TimeUnit.MINUTES, () -> commandEvent.replyWarning(ConstantStrings.EVENT_WAITER_TIMEOUT));
            }
        }
    }

    class StepTwoConsumer implements Consumer<MessageReceivedEvent> {
        private String command;
        private CommandEvent commandEvent;

        public StepTwoConsumer(CommandEvent commandEvent, String command) {
            this.command = command;
            this.commandEvent = commandEvent;
        }

        @Override
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
                    event.getTextChannel().getId(),
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
