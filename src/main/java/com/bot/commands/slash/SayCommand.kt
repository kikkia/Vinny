package com.bot.commands.slash

import com.bot.utils.VinnyConfig
import com.bot.voice.GuildVoiceProvider.Companion.getInstance
import com.jagrosh.jdautilities.command.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class SayCommand : BaseSlashCommand() {
    private val provider = getInstance()
    private val urlRegex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"

    init {
        this.name = "play"
        this.help = "Play an audio track in voice, either by url, or first result for the given search term."
        this.options = listOf(OptionData(OptionType.STRING, "track url or search", "what to play", true))
        this.guildOnly = true
    }

    override fun runCommand(command: SlashCommandEvent) {
        val conn = provider.getGuildVoiceConnection(command.guild!!)
        var url = command.optString("input")
        if (!url!!.matches(urlRegex.toRegex())) {
            val searchPrefix = VinnyConfig.instance().voiceConfig.defaultSearchProvider ?: "scsearch:"
            url = searchPrefix.plus(url)
        }

        command.reply("Loading [${url}]...").queue()
        conn.loadTrack(url, command)
    }
}
