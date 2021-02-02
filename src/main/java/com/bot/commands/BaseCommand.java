package com.bot.commands;

import com.bot.db.MembershipDAO;
import com.bot.exceptions.ForbiddenCommandException;
import com.bot.exceptions.PermsOutOfSyncException;
import com.bot.metrics.MetricsManager;
import com.bot.tasks.CommandTaskExecutor;
import com.bot.utils.CommandPermissions;
import com.bot.utils.Logger;
import com.bot.utils.ScheduledCommandUtils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.MDC;

import java.util.concurrent.ExecutorService;

public abstract class BaseCommand extends Command {
    protected MetricsManager metricsManager;
    protected Logger logger;
    protected MembershipDAO membershipDAO;
    protected ExecutorService commandExecutors;

    public boolean canSchedule;

    public BaseCommand() {
        this.metricsManager = MetricsManager.getInstance();
        this.logger = new Logger(this.getClass().getSimpleName());
        this.membershipDAO = MembershipDAO.getInstance();
        this.commandExecutors = CommandTaskExecutor.getTaskExecutor();
    }

    @Override
    @Trace(operationName = "pre-execute", resourceName = "baseCommand")
    protected void execute(CommandEvent commandEvent) {
        Guild guild = null;
        if (!commandEvent.isFromType(ChannelType.PRIVATE))
            guild = commandEvent.getGuild();

        metricsManager.markCommand(this, commandEvent.getAuthor(), guild);
        if (!ScheduledCommandUtils.isScheduled(commandEvent)) {
            commandEvent.getTextChannel().sendTyping().queue();
            membershipDAO.addUserToGuild(commandEvent.getMember().getUser(), commandEvent.getGuild());
        }

        // Check the permissions to do the command
        try {
            if (!CommandPermissions.canExecuteCommand(this, commandEvent))
                return;
        } catch (ForbiddenCommandException e) {
            commandEvent.replyWarning(e.getMessage());
            return;
        } catch (PermsOutOfSyncException e) {
            commandEvent.replyError("Could not find the role required for " + this.category.getName() + " commands. Please have a mod set a new role");
            Logger logger = new Logger(this.getClass().getName());
            logger.warning(e.getMessage() + " " + commandEvent.getGuild().getId());
            return;
        } catch (Exception e) {
            commandEvent.replyError("Something went wrong with permissions, please try again or go checkout the support server and report the bug.");
            e.printStackTrace();
            Logger logger = new Logger(this.getClass().getName());
            logger.severe("Failed to get perms for " + this.getClass().getName(), e);
            return;
        }
        // Add some details to the MDC on the thread before executing
        commandExecutors.submit(() -> {
            // Add some details to the MDC on the thread before executing
            try (MDC.MDCCloseable commandCloseable = MDC.putCloseable("command", this.name);
                 MDC.MDCCloseable argsCloseable = MDC.putCloseable("args", commandEvent.getArgs())){
                executeCommand(commandEvent);
            } catch (Exception e) {
                logger.severe("Exception Executing command", e);
                commandEvent.replyError("Something went wrong executing that command. If this continues please contact the support server.");
            }
        });
    }

    protected abstract void executeCommand(CommandEvent commandEvent);
}
