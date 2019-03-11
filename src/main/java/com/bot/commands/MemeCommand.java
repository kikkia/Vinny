package com.bot.commands;

import com.bot.utils.CommandCategories;
import com.jagrosh.jdautilities.command.Command;

public abstract class MemeCommand extends Command {

    public MemeCommand() {
        this.category = CommandCategories.MEME;
    }
}
