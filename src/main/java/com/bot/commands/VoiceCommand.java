package com.bot.commands;

import com.bot.utils.CommandCategories;
import com.bot.metrics.MetricsManager;
import com.jagrosh.jdautilities.command.Command;
import net.dv8tion.jda.core.Permission;

public abstract class VoiceCommand extends Command {
    protected MetricsManager metricsManager;

    public VoiceCommand() {
        this.category = CommandCategories.VOICE;
        this.guildOnly = true;
        this.botPermissions = new Permission[]{Permission.VOICE_CONNECT, Permission.VOICE_SPEAK};

        this.metricsManager = MetricsManager.getInstance();
    }
}
