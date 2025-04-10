package com.bot.commands.traditional.voice

import com.bot.Bot
import com.bot.commands.traditional.VoiceCommand
import com.bot.db.PlaylistDAO
import com.bot.voice.BaseAudioTrack
import com.bot.voice.QueuedAudioTrack
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import java.util.*

class SaveGuildPlaylistCommand(bot: Bot) : VoiceCommand() {
    private val playlistDAO: PlaylistDAO
    private val bot: Bot

    init {
        name = "savegplaylist"
        arguments = "<playlist name>"
        help = "Saves all of the currently queued tracks into a playlist tied to your guild."
        playlistDAO = PlaylistDAO.getInstance()
        this.bot = bot
    }

    @Trace(operationName = "executeCommand", resourceName = "SaveGuildPlaylist")
    override fun executeCommand(commandEvent: CommandEvent) {
        val args = commandEvent.args
        if (args.isEmpty()) {
            commandEvent.reply("You need to specify a name for the playlist.")
            return
        }
        val conn = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)

        val tracks = conn.getQueuedTracks()
        val nowPlaying = conn.nowPlaying()
        val trackList: MutableList<BaseAudioTrack> = LinkedList()

        if (nowPlaying == null) {
            commandEvent.replyWarning("I am not playing any tracks.")
            return
        }

        trackList.add(nowPlaying)
        trackList.addAll(tracks)
        if (playlistDAO.createPlaylistForGuild(commandEvent.guild.id, args, trackList)) {
            commandEvent.reply("Playlist successfully created.")
        } else {
            commandEvent.reply("Something went wrong! Failed to create playlist.")
            metricsManager.markCommandFailed(this, commandEvent.author, commandEvent.guild)
        }
    }
}