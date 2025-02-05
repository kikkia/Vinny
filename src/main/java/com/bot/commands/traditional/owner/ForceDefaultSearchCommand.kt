package com.bot.commands.traditional.owner

import com.bot.commands.traditional.OwnerCommand
import com.bot.utils.VinnyConfig
import com.jagrosh.jdautilities.command.CommandEvent

class ForceDefaultSearchCommand: OwnerCommand() {

    init {
        this.name = "forcesearch"
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        val vConfig = VinnyConfig.instance().voiceConfig
        vConfig.forceDefaultSearch = !vConfig.forceDefaultSearch
        commandEvent!!.reply("Set to: ${vConfig.forceDefaultSearch}")
    }
}