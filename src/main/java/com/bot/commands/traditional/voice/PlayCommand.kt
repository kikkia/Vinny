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
        commandEvent.channel.sendMessage(formatLoadingMessage(commandEvent.args)).complete()
        guildVoiceConnection.loadTrack(url, TextControlEvent(commandEvent))
    }

    companion object {
        private const val MAX_DISPLAY_ARGS_LENGTH = 200

        @JvmStatic
        fun formatLoadingMessage(args: String): String {
            val singleLine = args.replace("\r", " ").replace("\n", " ").trim()
            val display = if (singleLine.length > MAX_DISPLAY_ARGS_LENGTH) {
                singleLine.substring(0, MAX_DISPLAY_ARGS_LENGTH) + "..."
            } else {
                singleLine
            }
            return "\u231A Loading... `[$display]`"
        }
    }
}