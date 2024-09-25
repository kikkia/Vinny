package com.bot.commands.voice

import com.bot.Bot
import com.bot.commands.VoiceCommand
import com.bot.utils.VinnyConfig
import com.bot.voice.UrlTranslationUtils
import com.bot.voice.VoiceProviderClient
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import net.dv8tion.jda.api.entities.Message

class PlayCommand(private val bot: Bot) : VoiceCommand() {

    private val urlRegex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    private val audioProvidersEnabled = VinnyConfig.instance().voiceConfig.voiceProviderAPI != null

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
            // TODO: HACKY right now for quick experimentation
            if (audioProvidersEnabled) {
                commandEvent.replyWarning("Currently searching is disabled while we continue to find fixes to voice. " +
                        "Please use the URL of whatever you want to play.")
                return
            }
            val searchPrefix = VinnyConfig.instance().voiceConfig.defaultSearchProvider ?: "scsearch:"
            url = searchPrefix.plus(commandEvent.args)
        }

        if (audioProvidersEnabled) {
            val urlParseResult = UrlTranslationUtils.translateUrl(url)
            if (!urlParseResult.valid) {
                commandEvent.replyWarning("That URL is not valid, please ensure you are using a supported site. " +
                        "Please reach out on the support server if you believe the url should work.")
                return
            }

            if (urlParseResult.useProvider) {
                val newURl = VinnyConfig.instance().voiceConfig.voiceProviderFormat!!.format(urlParseResult.extractedId)
                guildVoiceConnection.loadProviderTrack(newURl, commandEvent, VoiceProviderClient.getProviders())
            }
        }

        commandEvent.reply("\u231A Loading... `[" + commandEvent.args + "]`") { _: Message? ->
            guildVoiceConnection.loadTrack(url, commandEvent)
        }
    }
}