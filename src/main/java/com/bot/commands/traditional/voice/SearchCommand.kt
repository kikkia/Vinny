package com.bot.commands.traditional.voice

import com.bot.Bot
import com.bot.commands.traditional.VoiceCommand
import com.bot.commands.control.TextControlEvent
import com.bot.exceptions.newstyle.UserVisibleException
import com.bot.utils.VinnyConfig
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.menu.OrderedMenu
import datadog.trace.api.Trace
import java.util.concurrent.TimeUnit

class SearchCommand(bot: Bot, eventWaiter: EventWaiter?) : VoiceCommand() {
    private val bot: Bot
    private val builder: OrderedMenu.Builder
    private val voiceConfig = VinnyConfig.instance().voiceConfig

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
        if (commandEvent.member!!.voiceState == null || !commandEvent.member!!.voiceState!!.inAudioChannel()) {
            throw UserVisibleException("VOICE_NOT_IN_CHANNEL")
        }
        val controlEvent = TextControlEvent(commandEvent)
        guildVoiceConnection.joinChannel(controlEvent)

        var searchPrefix = voiceConfig.defaultSearchProvider
        if (guildVoiceConnection.oauthConfig != null && !voiceConfig.forceDefaultSearch) {
            searchPrefix = voiceConfig.loginSearchProvider
        }
        val search = searchPrefix.plus(commandEvent.args)
        val message = commandEvent.channel.sendMessage("Searching for `[" + commandEvent.args + "]`").complete()
        guildVoiceConnection.searchForTrack(search, controlEvent, message, builder)
    }
}