package com.bot.commands.slash.nsfw

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.service.E621Service
import com.bot.utils.TheGreatCCPFilter.Companion.containsNoNoTags
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import kotlin.random.Random

class E621SlashCommand: NsfwSlashCommand() {

    private val e621Service: E621Service

    init {
        this.name = "e621"
        this.help = "Posts from e621 to the channel"
        this.options = listOf(
            OptionData(OptionType.STRING, "search", "What to search for", true, true),
            OptionData(OptionType.STRING, "sort", "Sort by", false, true)
        )
        this.e621Service = E621Service.getInstance()
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        var search = command.optString("search")!!.replace(" ", "+");
        val order = command.optString("sort")
        if (order != null) {
            search += "+$order"
        }

        if (containsNoNoTags(search)) {
            command.replyWarning("DISCORD_BANNED_TERMS")
            return
        }
        val images: List<String> = e621Service.getPostsForSearch(search)
        val selected = images[Random.nextInt(images.size - 1)]
        val refreshButtonId = "refresh-e621-$search"
        val refresh = Button.primary(refreshButtonId, Emoji.fromUnicode("\uD83D\uDD04"))
        command.replyToCommand(selected, mutableListOf(refresh))
    }

    private val sortOptions = listOf(
        Choice("Newest", "order:id"),
        Choice("Score", "order:score"),
        Choice("Favorite Count", "order:favcount"),
    )


    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent?) {
        var choices: List<Choice>? = null
        if (event!!.focusedOption.name == "sort") {
            choices = sortOptions
        } else if (event.focusedOption.name == "search") {
            choices = tagsToChoice(e621Service.getAutocomplete(event.focusedOption.value))
        }
        if (choices != null) {
            event.replyChoices(choices).queue()
        }
        super.onAutoComplete(event)
    }

    private fun tagsToChoice(tags: List<String>) : List<Choice> {
        return tags.map { Choice(it, it) }
    }
}