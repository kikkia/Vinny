package com.bot.commands.slash.nsfw

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.utils.R34Utils
import com.bot.utils.TheGreatCCPFilter.Companion.containsNoNoTags
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.buttons.Button

class R34SlashCommand: NsfwSlashCommand() {

    init {
        this.name = "rule34"
        this.help = "Posts rule 34 with given tags to the channel"
        this.options = listOf(
            OptionData(OptionType.STRING, "search", "What to search for", true)
        )
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val search = command.optString("search")!!.replace(" ", "+");

        if (containsNoNoTags(search)) {
            command.replyWarning("DISCORD_BANNED_TERMS")
            return
        }
        val refreshButton = Button.primary("refresh-r34-$search", Emoji.fromUnicode("\uD83D\uDD04"))
        command.replyToCommand(R34Utils.getPostForSearch(search), mutableListOf(refreshButton))
    }
}