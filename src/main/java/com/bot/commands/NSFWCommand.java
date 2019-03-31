package com.bot.commands;

import com.bot.exceptions.ForbiddenCommandException;
import com.bot.utils.CommandCategories;
import com.bot.metrics.MetricsManager;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.Permission;

public abstract class NSFWCommand extends Command {
    protected MetricsManager metricsManager;

    public NSFWCommand() {
        this.category = CommandCategories.NSFW;
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};

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
