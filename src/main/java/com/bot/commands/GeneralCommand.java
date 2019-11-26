package com.bot.commands;

import com.bot.utils.CommandCategories;
import net.dv8tion.jda.api.Permission;

public abstract class GeneralCommand extends BaseCommand {

    public GeneralCommand() {
        this.category = CommandCategories.GENERAL;
        this.guildOnly = true;
        this.ownerCommand = false;
        this.hidden = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION};
    }
}
