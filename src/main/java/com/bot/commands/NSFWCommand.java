package com.bot.commands;

import com.bot.utils.CommandCategories;
import net.dv8tion.jda.api.Permission;

public abstract class NSFWCommand extends BaseCommand {

    public NSFWCommand() {
        this.category = CommandCategories.NSFW;
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS};
    }
}
