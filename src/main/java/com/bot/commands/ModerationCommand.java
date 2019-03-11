package com.bot.commands;

import com.bot.utils.CommandCategories;
import com.jagrosh.jdautilities.command.Command;

public abstract class ModerationCommand extends Command {

    public ModerationCommand() {
        this.category = CommandCategories.MODERATION;
        this.guildOnly = true;
    }
}
