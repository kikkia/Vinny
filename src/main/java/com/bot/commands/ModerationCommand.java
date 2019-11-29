package com.bot.commands;

import com.bot.utils.CommandCategories;
import net.dv8tion.jda.api.Permission;

public abstract class ModerationCommand extends BaseCommand {
    public ModerationCommand() {
        this.category = CommandCategories.MODERATION;
        this.guildOnly = true;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION};
    }
}
