package com.bot.commands;

import com.jagrosh.jdautilities.command.Command;

public abstract class OwnerCommand extends Command {
    public OwnerCommand() {
        this.ownerCommand = true;
        this.guildOnly = false;
        this.hidden = true;
    }
}
