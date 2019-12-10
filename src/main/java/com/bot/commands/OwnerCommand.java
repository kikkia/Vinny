package com.bot.commands;

import com.bot.utils.CommandCategories;

public abstract class OwnerCommand extends BaseCommand {

    public OwnerCommand() {
        this.ownerCommand = true;
        this.guildOnly = false;
        this.hidden = true;
        this.category = CommandCategories.OWNER;
    }
}
