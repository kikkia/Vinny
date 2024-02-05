package com.bot.interactions.commands

import com.bot.utils.CommandCategories
import com.bot.voice.GuildVoiceProvider
import net.dv8tion.jda.api.Permission

abstract class VoiceCommand : BaseCommandText() {
    val guildVoiceProvider = GuildVoiceProvider.getInstance()
    init {
        this.category = CommandCategories.VOICE
        this.guildOnly = true
        this.botPermissions = arrayOf(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ADD_REACTION)
        this.canSchedule = false
    }
}
