package com.bot.commands.voice

import com.bot.Bot
import com.bot.commands.VoiceCommand
import com.bot.exceptions.MaxQueueSizeException
import com.bot.utils.Config
import com.bot.utils.FormattingUtils
import com.bot.voice.GuildVoiceProvider
import com.bot.voice.LavaLinkClient
import com.bot.voice.LavaLinkClient.Companion.getInstance
import com.bot.voice.VoiceSendHandler
import com.jagrosh.jdautilities.command.CommandEvent
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import datadog.trace.api.Trace
import dev.arbjerg.lavalink.client.LinkState
import net.dv8tion.jda.api.entities.Message
import java.util.concurrent.TimeUnit

class PlayCommand(private val bot: Bot) : VoiceCommand() {

    private val urlRegex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"

    init {
        name = "play"
        arguments = "<title|URL>"
        help = "plays the provided audio track"
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
            val searchPrefix = Config.getInstance().getConfig(Config.DEFAULT_SEARCH_PROVIDER, "ytsearch:")
            url = searchPrefix.plus(url)
        }
        commandEvent.reply("\u231A Loading... `[" + commandEvent.args + "]`") { _: Message? ->
            guildVoiceConnection.loadTrack(url, commandEvent)
        }
    }

    private inner class PlayHandler private constructor(
        private val message: Message,
        private val source: String,
        private val commandEvent: CommandEvent,
        private val search: Boolean
    ) : AudioLoadResultHandler {
        @Throws(MaxQueueSizeException::class)
        private fun loadTracks(track: AudioTrack?, playlist: List<AudioTrack>?, fromList: Boolean): Boolean {
            return if (playlist == null) {
                if (VoiceSendHandler.isSongTooLong(track)) {
                    message.editMessage(
                        commandEvent.client.warning + " The track was longer than the max length of " +
                                FormattingUtils.msToMinSec(VoiceSendHandler.MAX_DURATION * 1000)
                    ).queue()
                    return false
                }
                // If the queue track was successful go on, if not return.
                if (bot.queueTrack(track, commandEvent, message)) {
                    if (!fromList) message.editMessage(commandEvent.client.success + " Successfully added `" + track!!.info.title + "` to queue.")
                        .queue()
                    true
                } else {
                    message.editMessage(commandEvent.client.error + " Failed to add track to playlist.").queue()
                    false
                }
            } else {
                // This will only happen with a playlist
                var count = 0
                val tracksAdded: MutableList<String> = ArrayList()
                for (t in playlist) {
                    if (loadTracks(t, null, true)) {
                        count++
                        tracksAdded.add(t.info.title)
                    } else {
                        message.editMessage(commandEvent.client.error + " Failed to add a track to playlist. Added " + count + " tracks.")
                            .queue()
                        return false
                    }
                }
                if (!tracksAdded.isEmpty()) {
                    commandEvent.reply("Added the following tracks: " + prettyPrintTracks(tracksAdded))
                }
                true
            }
        }

        override fun trackLoaded(audioTrack: AudioTrack) {
            try {
                loadTracks(audioTrack, null, false)
            } catch (e: MaxQueueSizeException) {
                message.editMessage(e.message!!).queue()
            }
        }

        override fun playlistLoaded(audioPlaylist: AudioPlaylist) {
            if (audioPlaylist.isSearchResult) {
                val track = audioPlaylist.tracks[0]
                try {
                    loadTracks(track, null, false)
                } catch (e: MaxQueueSizeException) {
                    message.editMessage(e.message!!).queue()
                }
                return
            }
            // They gave multiple args, assume one is the tracks.
            var trackNums = arrayOf<String>()
            if (commandEvent.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray().size == 2) trackNums =
                commandEvent.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (trackNums.size == 2) {
                var to: Int
                val from: Int
                try {
                    from = trackNums[0].toInt() - 1 // Account for zero index
                    to = trackNums[1].toInt()
                } catch (e: NumberFormatException) {
                    commandEvent.reply(commandEvent.client.warning + "  NumberFormatException: Invalid number given, please only user numeric characters.")
                    return
                }
                if (from > to) {
                    commandEvent.reply(commandEvent.client.warning + " Error: Beginning index is bigger than ending index.")
                    return
                } else if (from < 0) {
                    commandEvent.reply(commandEvent.client.warning + " Error: Beginning index is less than 1.")
                    return
                } else if (to > audioPlaylist.tracks.size) {
                    commandEvent.reply(commandEvent.client.warning + " Error: Requesting tracks out of range. Only " + audioPlaylist.tracks.size + " tracks in playlist :x:")
                    return
                } else if (to - from > 9) {
                    commandEvent.reply(
                        commandEvent.client.warning + " Warning: Requesting number of tracks that is greater than 10. Trimming results to " +
                                "" + from + "-" + (from + 9) + " :exclamation:"
                    )
                    to = from + 9
                }
                try {
                    loadTracks(null, audioPlaylist.tracks.subList(from, to), true)
                } catch (e: MaxQueueSizeException) {
                    message.editMessage(e.message!!).queue()
                }
            } else {
                try {
                    loadTracks(null, audioPlaylist.tracks, true)
                } catch (e: MaxQueueSizeException) {
                    message.editMessage(e.message!!).queue()
                }
            }
        }

        override fun noMatches() {
            message.editMessage(commandEvent.client.warning + " Failed to find a track for " + commandEvent.args)
                .queue()
        }

        override fun loadFailed(e: FriendlyException) {
            message.editMessage(commandEvent.client.error + " I encountered an error loading track: " + e.message)
                .queue()
            logger.warning("Failed to load a track", e)
        }

        private fun prettyPrintTracks(tracks: List<String>): String {
            var prettyString = "```"
            for (s in tracks) {
                prettyString += """
                    $s
                    
                    """.trimIndent()
            }
            prettyString += "```"
            return prettyString
        }
    }
}