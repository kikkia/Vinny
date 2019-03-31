package com.bot.commands;

import com.bot.exceptions.ForbiddenCommandException;
import com.bot.utils.CommandCategories;
import com.bot.metrics.MetricsManager;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public abstract class ModerationCommand extends Command {
    protected MetricsManager metricsManager;

    public ModerationCommand() {
        this.category = CommandCategories.MODERATION;
        this.guildOnly = true;

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
        }

        executeCommand(commandEvent);
    }

    protected abstract void executeCommand(CommandEvent commandEvent);
}
