package com.bot.commands;

import com.bot.utils.CommandCategories;
import com.jagrosh.jdautilities.command.Command;

public abstract class RedditCommand extends Command {

    public RedditCommand() {
        this.category = CommandCategories.REDDIT;
        this.guildOnly = false;
    }
}
