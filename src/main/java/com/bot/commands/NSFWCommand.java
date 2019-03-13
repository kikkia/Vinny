package com.bot.commands;

import com.bot.utils.CommandCategories;
import com.bot.utils.MetricsManager;
import com.jagrosh.jdautilities.command.Command;
import net.dv8tion.jda.core.Permission;

public abstract class NSFWCommand extends Command {
    protected MetricsManager metricsManager;

    public NSFWCommand() {
        this.category = CommandCategories.NSFW;
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};

        this.metricsManager = MetricsManager.getInstance();
    }
}
