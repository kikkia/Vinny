package com.bot.commands;

import com.bot.utils.CommandCategories;
import com.bot.utils.MetricsManager;
import com.jagrosh.jdautilities.command.Command;

public abstract class ModerationCommand extends Command {
    protected MetricsManager metricsManager;

    public ModerationCommand() {
        this.category = CommandCategories.MODERATION;
        this.guildOnly = true;

        this.metricsManager = MetricsManager.getInstance();
    }
}
