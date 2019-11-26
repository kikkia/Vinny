package com.bot.commands;

import com.bot.utils.CommandCategories;

public abstract class MemeCommand extends BaseCommand {

    public MemeCommand() {
        this.category = CommandCategories.MEME;
    }
}
