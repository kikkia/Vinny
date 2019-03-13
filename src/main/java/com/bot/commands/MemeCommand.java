package com.bot.commands;

import com.bot.utils.CommandCategories;
import com.bot.utils.MetricsManager;
import com.jagrosh.jdautilities.command.Command;

public abstract class MemeCommand extends Command {
    protected MetricsManager metricsManager;

    public MemeCommand() {
        this.category = CommandCategories.MEME;

        this.metricsManager = MetricsManager.getInstance();
    }
}
