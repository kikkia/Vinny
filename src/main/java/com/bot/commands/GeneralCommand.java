package com.bot.commands;

import com.bot.utils.CommandCategories;
import com.jagrosh.jdautilities.command.Command;

public abstract class GeneralCommand extends Command {

    public GeneralCommand() {
        this.category = CommandCategories.GENERAL;
        this.guildOnly = true;
        this.ownerCommand = false;
        this.hidden = false;
    }
}
