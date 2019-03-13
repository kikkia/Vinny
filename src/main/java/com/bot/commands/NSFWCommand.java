package com.bot.commands;

import com.bot.utils.CommandCategories;
import com.jagrosh.jdautilities.command.Command;
import net.dv8tion.jda.core.Permission;

public abstract class NSFWCommand extends Command {

    public NSFWCommand() {
        this.category = CommandCategories.NSFW;
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
    }
}
