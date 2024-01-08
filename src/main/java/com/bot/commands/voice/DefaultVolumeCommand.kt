package com.bot.commands.voice

import com.bot.commands.ModerationCommand
import com.bot.db.GuildDAO
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class DefaultVolumeCommand : ModerationCommand() {
    private val guildDAO: GuildDAO

    init {
        name = "dvolume"
        arguments = "<Volume 1-200>"
        help = "Sets the default volume for the server"
        guildDAO = GuildDAO.getInstance()
    }

    @Trace(operationName = "executeCommand", resourceName = "dvolume")
    override fun executeCommand(commandEvent: CommandEvent) {
        val newVolume: Int
        try {
            newVolume = commandEvent.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toInt()
            if (newVolume > 200 || newVolume < 0) {
                throw NumberFormatException()
            }
            if (!guildDAO.updateGuildVolume(commandEvent.guild.id, newVolume)) {
                commandEvent.reply(commandEvent.client.error + " Something went wrong updating the default volume.")
                metricsManager.markCommandFailed(this, commandEvent.author, commandEvent.guild)
                return
            }
            commandEvent.reactSuccess()
        } catch (e: NumberFormatException) {
            commandEvent.reply(commandEvent.client.warning + " You must enter a volume between 0 and 200")
        }
    }
}