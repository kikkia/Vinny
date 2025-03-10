package com.bot.commands.traditional.voice

import com.bot.commands.control.SlashControlEvent
import com.bot.commands.traditional.VoiceCommand
import com.bot.utils.VinnyConfig
import com.bot.commands.control.TextControlEvent
import com.bot.exceptions.newstyle.UserVisibleException
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.CooldownScope
import datadog.trace.api.Trace

class PlayCommand: VoiceCommand() {

    private val urlRegex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    private val voiceConfig = VinnyConfig.instance().voiceConfig

    init {
        name = "play"
        arguments = "<title|URL>"
        aliases = arrayOf("p")
        help = "plays the provided audio track"
        cooldown = 3
        cooldownScope = CooldownScope.USER
    }

    @Trace(operationName = "executeCommand", resourceName = "Play")
    override fun executeCommand(commandEvent: CommandEvent) {
        val guildVoiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        // Ensure that the user is in a voice channel before using
        if (commandEvent.member!!.voiceState == null || !commandEvent.member!!.voiceState!!.inAudioChannel()) {
            throw UserVisibleException("VOICE_NOT_IN_CHANNEL")
        }
        guildVoiceConnection.joinChannel(TextControlEvent(commandEvent))

        if (commandEvent.args.isEmpty()) {
            if (guildVoiceConnection.getPaused()) {
                guildVoiceConnection.setPaused(false)
                commandEvent.reply(commandEvent.client.success + " Resumed paused stream.")
            } else {
                commandEvent.reply(
                    """${commandEvent.client.warning} You must give me something to play.
                    `${commandEvent.client.prefix}play <URL>` - Plays media at the provided URL
                    `${commandEvent.client.prefix}play <search term>` - Searches youtube for the first result of the search term"""
                )
            }
            return
        }
        var url = commandEvent.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        if (!url.matches(urlRegex.toRegex())) {
            var searchPrefix = voiceConfig.defaultSearchProvider
            if (guildVoiceConnection.oauthConfig != null && !voiceConfig.forceDefaultSearch) {
                searchPrefix = voiceConfig.loginSearchProvider
            }
            url = searchPrefix.plus(commandEvent.args)
        }
        commandEvent.channel.sendMessage("\u231A Loading... `[" + commandEvent.args + "]`").complete()
        guildVoiceConnection.loadTrack(url, TextControlEvent(commandEvent))
    }
}