package com.bot.commands;

import com.bot.exceptions.ForbiddenCommandException;
import com.bot.exceptions.PermsOutOfSyncException;
import com.bot.metrics.MetricsManager;
import com.bot.utils.CommandCategories;
import com.bot.utils.CommandPermissions;
import com.bot.utils.Logger;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.Permission;

public abstract class GeneralCommand extends Command {
    protected MetricsManager metricsManager;

    public GeneralCommand() {
        this.category = CommandCategories.GENERAL;
        this.guildOnly = true;
        this.ownerCommand = false;
        this.hidden = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION};


        metricsManager = MetricsManager.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());

        // Check the permissions to do the command
        try {
            if (!CommandPermissions.canExecuteCommand(this, commandEvent))
                return;
        } catch (ForbiddenCommandException e) {
            commandEvent.replyWarning(e.getMessage());
            return;
        } catch (PermsOutOfSyncException e) {
            commandEvent.replyError("Could not find the role required for " + this.category.getName() + " commands. Please have a mod set a new role.");
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

        commandEvent.async(() -> executeCommand(commandEvent));
    }

    protected abstract void executeCommand(CommandEvent commandEvent);
}
