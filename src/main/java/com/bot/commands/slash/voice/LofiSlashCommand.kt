package com.bot.commands.slash.voice

import com.bot.commands.control.SlashControlEvent
import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.voice.radio.LofiRadioService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class LofiSlashCommand: VoiceSlashCommand() {

    init {
        this.name = "lofi"
        this.help = "Play a lofi radio station"
        this.options = listOf(
            OptionData(OptionType.STRING, "station", "Station to play", true, true)
        )
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val station = command.optString("station")!!
        provider.getGuildVoiceConnection(command.guild!!).setRadio(station, SlashControlEvent(command))
    }

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent?) {
        val choices: List<Choice> = LofiRadioService.radioStations.map { Choice(it.value.name, it.key) }
        if (choices.isNotEmpty()) {
            event!!.replyChoices(choices).queue()
        }
        super.onAutoComplete(event)
    }
}