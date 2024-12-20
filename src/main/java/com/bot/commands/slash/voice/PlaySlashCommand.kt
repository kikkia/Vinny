package com.bot.commands.slash.voice

import com.bot.commands.slash.BaseSlashCommand
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.utils.CommandCategories
import com.bot.utils.VinnyConfig
import com.bot.voice.GuildVoiceProvider
import com.bot.voice.control.VoiceSlashControlEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class PlaySlashCommand: BaseSlashCommand() {
    private val urlRegex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    private val guildVoiceProvider = GuildVoiceProvider.getInstance()

    init {
        this.name = "play"
        this.help = "Play a track in voice"
        this.category = CommandCategories.VOICE
        this.options = listOf(OptionData(OptionType.STRING, "url-or-search", "url of track or search term", true, false))
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val guildVoiceConnection = guildVoiceProvider.getGuildVoiceConnection(command.guild!!)

        val input = command.optString("url-or-search")
        var url = input!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        if (!url.matches(urlRegex.toRegex())) {
            val searchPrefix = VinnyConfig.instance().voiceConfig.defaultSearchProvider ?: "scsearch:"
            url = searchPrefix.plus(input)
        }
        command.replyToCommand("\u231A Loading... `[" + input + "]`")
        guildVoiceConnection.loadTrack(url, VoiceSlashControlEvent(command))
    }
}