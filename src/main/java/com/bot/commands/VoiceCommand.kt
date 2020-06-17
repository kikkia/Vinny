package com.bot.commands

import com.bot.utils.CommandCategories
import net.dv8tion.jda.api.Permission

abstract class VoiceCommand : BaseCommand() {
    init {
        this.category = CommandCategories.VOICE
        this.guildOnly = true
        this.botPermissions = arrayOf(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION)
        this.canSchedule = false
    }
}
