package com.bot.commands;

import com.bot.metrics.MetricsManager;
import com.bot.utils.Logger;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public abstract class OwnerCommand extends Command {
    protected MetricsManager metricsManager;
    protected Logger logger;

    public OwnerCommand() {
        this.ownerCommand = true;
        this.guildOnly = false;
        this.hidden = true;

        this.metricsManager = MetricsManager.getInstance();
        this.logger = new Logger(this.getClass().getSimpleName());
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());

        try {
            commandEvent.async(() -> executeCommand(commandEvent));
        } catch (Exception e) {
            commandEvent.replyError("Something went wrong, please try again later");
            logger.severe("Failed command " + this.getClass().getName() + ": ", e);
            e.printStackTrace();
        }
    }

    protected abstract void executeCommand(CommandEvent commandEvent);
}
