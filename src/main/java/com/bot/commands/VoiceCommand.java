package com.bot.commands;

import com.bot.utils.CommandCategories;
import net.dv8tion.jda.api.Permission;

public abstract class VoiceCommand extends BaseCommand {

    public VoiceCommand() {
        this.category = CommandCategories.VOICE;
        this.guildOnly = true;
        this.botPermissions = new Permission[]{Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION};
    }
}
