package com.bot.commands;

import com.bot.exceptions.ForbiddenCommandException;
import com.bot.metrics.MetricsManager;
import com.bot.utils.CommandPermissions;
import com.bot.utils.Logger;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public abstract class OwnerCommand extends Command {
    protected MetricsManager metricsManager;

    public OwnerCommand() {
        this.ownerCommand = true;
        this.guildOnly = false;
        this.hidden = true;

        this.metricsManager = MetricsManager.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Check the permissions to do the command
        try {
            if (!CommandPermissions.canExecuteCommand(this, commandEvent))
                return;
        } catch (ForbiddenCommandException e) {
            commandEvent.replyWarning(e.getMessage());
            return;
        } catch (Exception e) {
            commandEvent.replyError("Something went wrong parsing permissions, please try again later.");
            Logger logger = new Logger(this.getClass().getName());
            logger.severe("Failed command " + this.getClass().getName() + ": ", e);
            e.printStackTrace();
        }

        try {
            executeCommand(commandEvent);
        } catch (Exception e) {
            commandEvent.replyError("Something went wrong, please try again later");
            Logger logger = new Logger(this.getClass().getName());
            logger.severe("Failed command " + this.getClass().getName() + ": ", e);
            e.printStackTrace();
        }
    }

    protected abstract void executeCommand(CommandEvent commandEvent);
}
