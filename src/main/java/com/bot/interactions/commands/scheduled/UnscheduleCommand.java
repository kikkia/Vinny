package com.bot.interactions.commands.scheduled;

import com.bot.interactions.InteractionEvent;
import com.bot.interactions.commands.ModerationCommand;
import com.bot.db.ScheduledCommandDAO;
import com.bot.utils.ConstantStrings;
import com.bot.utils.ScheduledCommandUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class UnscheduleCommand extends ModerationCommand {

    private final ScheduledCommandDAO scheduledCommandDAO;
    private final EventWaiter waiter;

    public UnscheduleCommand(EventWaiter waiter) {
        this.name = "unschedule";
        this.help = "Unschedules a command";
        this.guildOnly = true;
        this.arguments = "<Scheduled command ID or none>";
        this.waiter = waiter;
        this.scheduledCommandDAO = ScheduledCommandDAO.getInstance();
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "Unschedule")
    protected void executeCommand(CommandEvent commandEvent) {
        if (commandEvent.getArgs().isEmpty()) {
            commandEvent.reply("Please just respond with the id of the scheduled command you want to remove? You can find this id" +
                    "using the `~scheduled` command. For example, `~scheduled me` gets all commands you have scheduled.");

            waiter.waitForEvent(MessageReceivedEvent.class,
                    e -> e.getAuthor().equals(commandEvent.getAuthor())
                            && e.getChannel().equals(commandEvent.getChannel())
                            && !e.getMessage().equals(commandEvent.getMessage()),
                    new StepOneConsumer(commandEvent),
                    // if the user takes more than a minute, time out
                    1, TimeUnit.MINUTES, () -> commandEvent.reply(ConstantStrings.EVENT_WAITER_TIMEOUT));
        }
        else {
            ScheduledCommandUtils.deleteScheduledCommand(commandEvent, commandEvent.getArgs());
        }
    }

    @Override
    protected void executeCommand(InteractionEvent commandEvent) {

    }

    class StepOneConsumer implements Consumer<MessageReceivedEvent> {

        private final CommandEvent commandEvent;

        public StepOneConsumer(CommandEvent commandEvent) {
            this.commandEvent = commandEvent;
        }

        @Override
        @Trace(operationName = "executeCommand", resourceName = "Unscheduled.stepOne")
        public void accept(MessageReceivedEvent event) {
            // If they reply with command with arg, just ignore
            if (event.getMessage().getContentRaw().split(" ").length > 1)
                return;

            ScheduledCommandUtils.deleteScheduledCommand(commandEvent, event.getMessage().getContentRaw());
        }
    }
}
