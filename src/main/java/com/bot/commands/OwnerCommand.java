package com.bot.commands;

import com.bot.utils.MetricsManager;
import com.jagrosh.jdautilities.command.Command;

public abstract class OwnerCommand extends Command {
    protected MetricsManager metricsManager;

    public OwnerCommand() {
        this.ownerCommand = true;
        this.guildOnly = false;
        this.hidden = true;

        this.metricsManager = MetricsManager.getInstance();
    }
}
