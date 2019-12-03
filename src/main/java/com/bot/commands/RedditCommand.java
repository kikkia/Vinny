package com.bot.commands;

import com.bot.utils.CommandCategories;
import net.dv8tion.jda.api.Permission;

public abstract class RedditCommand extends BaseCommand {

    public RedditCommand() {
        this.category = CommandCategories.REDDIT;
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS};
        this.cooldownScope = CooldownScope.USER;
        this.cooldown = 1;
    }
}
