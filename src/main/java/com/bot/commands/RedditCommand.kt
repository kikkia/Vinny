package com.bot.commands

import com.bot.utils.CommandCategories
import com.jagrosh.jdautilities.command.Command
import net.dv8tion.jda.api.Permission

abstract class RedditCommand : BaseCommand() {
    init {
        this.category = CommandCategories.REDDIT
        this.guildOnly = false
        this.botPermissions = arrayOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS)
        this.cooldownScope = Command.CooldownScope.USER
        this.cooldown = 1
    }
}
