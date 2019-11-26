package com.bot.commands;

public abstract class OwnerCommand extends BaseCommand {

    public OwnerCommand() {
        this.ownerCommand = true;
        this.guildOnly = false;
        this.hidden = true;
    }
}
