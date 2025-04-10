package com.bot.commands.traditional.owner

import com.bot.commands.traditional.OwnerCommand
import com.bot.voice.radio.LofiRadioService
import com.jagrosh.jdautilities.command.CommandEvent

class TestLofiCommand : OwnerCommand() {

    init {
        name = "lofi"
    }

    override fun executeCommand(commandEvent: CommandEvent) {
        var content = ""
        for (station in LofiRadioService.radioStations.values) {
            content += "${station.name} - ${station.getNowPlaying().title}\n"
        }
        commandEvent.reply(content)
    }
}