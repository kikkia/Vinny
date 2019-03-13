package com.bot.commands;

import com.bot.utils.CommandCategories;
import com.bot.utils.MetricsManager;
import com.jagrosh.jdautilities.command.Command;

public abstract class RedditCommand extends Command {
    protected MetricsManager metricsManager;

    public RedditCommand() {
        this.category = CommandCategories.REDDIT;
        this.guildOnly = false;

        this.metricsManager = MetricsManager.getInstance();
    }
}
