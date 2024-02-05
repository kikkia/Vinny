package com.bot.interactions.commands.voice

import com.bot.Bot
import com.bot.interactions.commands.VoiceCommand
import com.bot.db.PlaylistDAO
import com.bot.interactions.InteractionEvent
import com.bot.voice.QueuedAudioTrack
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import java.util.*

class SaveMyPlaylistCommand(private val bot: Bot) : VoiceCommand() {
    private val playlistDAO: PlaylistDAO

    init {
        name = "savemyplaylist"
        arguments = "Name"
        help = "Saves the current audio playlist as a playlist accessible for any server you are on."
        playlistDAO = PlaylistDAO.getInstance()
    }

    @Trace(operationName = "executeCommand", resourceName = "SaveMyPlaylist")
    override fun executeCommand(commandEvent: CommandEvent) {
        val args = commandEvent.args
        if (args.isEmpty()) {
            commandEvent.reply("You need to specify a name for the playlist.")
            return
        }
        val conn = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)

        val tracks = conn.getQueuedTracks()
        val nowPlaying = conn.nowPlaying()
        val trackList: MutableList<QueuedAudioTrack> = LinkedList()

        if (nowPlaying == null) {
            commandEvent.replyWarning("I am not playing any tracks.")
            return
        }

        trackList.add(nowPlaying)
        trackList.addAll(tracks)
        if (playlistDAO.createPlaylistForUser(commandEvent.author.id, args, trackList)) {
            commandEvent.reply("Playlist successfully created.")
        } else {
            commandEvent.reply("Something went wrong! Failed to create playlist.")
            metricsManager.markCommandFailed(this, commandEvent.author, commandEvent.guild)
        }
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}