package com.bot.commands.voice

import com.bot.Bot
import com.bot.commands.VoiceCommand
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.menu.OrderedMenu
import datadog.trace.api.Trace
import net.dv8tion.jda.api.entities.Message
import java.util.concurrent.TimeUnit

class SearchCommand(bot: Bot, eventWaiter: EventWaiter?) : VoiceCommand() {
    private val bot: Bot
    private val builder: OrderedMenu.Builder

    init {
        name = "search"
        arguments = "<Search terms>"
        help = "Searches for the track and replies with a list of tracks to play."
        builder = OrderedMenu.Builder()
            .useNumbers()
            .allowTextInput(false)
            .useCancelButton(true)
            .setEventWaiter(eventWaiter)
            .setTimeout(30, TimeUnit.SECONDS)
        this.bot = bot
    }

    @Trace(operationName = "executeCommand", resourceName = "SearchYT")
    override fun executeCommand(commandEvent: CommandEvent) {
        if (commandEvent.args.isEmpty()) {
            commandEvent.replyWarning("You need to give me something to search")
            return
        }

        val guildVoiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)

        val search = "ytsearch:".plus(commandEvent.args)
        commandEvent.reply(
            "Searching for `[" + commandEvent.args + "]`"
        ) { m: Message ->
            guildVoiceConnection.searchForTrack(search, commandEvent, m, builder)
        }
    }
}