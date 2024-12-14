package com.bot.commands.traditional.voice

import com.bot.commands.traditional.VoiceCommand
import com.bot.db.PlaylistDAO
import com.bot.models.Playlist
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import java.util.logging.Level

class LoadMyPlaylistCommand : VoiceCommand() {
    private val playlistDAO: PlaylistDAO

    init {
        name = "loadmyplaylist"
        arguments = "<playlist id|playlist name>"
        help = "Loads one of your playlists. You must either specify the id or the name of the playlist."
        playlistDAO = PlaylistDAO.getInstance()
    }

    @Trace(operationName = "executeCommand", resourceName = "LoadMyPlaylists")
    override fun executeCommand(commandEvent: CommandEvent) {
        var playlistId = -1
        var playlistName: String? = null
        val playlist: Playlist?
        try {
            // Check if we are given a number (implies playlist id)
            playlistId = commandEvent.args.toInt()
        } catch (e: NumberFormatException) {
            // if number parsing fails we look for the name;
            playlistName = commandEvent.args
        }
        if (playlistName != null && playlistName.isEmpty()) {
            commandEvent.replyWarning("You must specify a playlist name or id to load it.")
            return
        }
        val userId = commandEvent.author.id
        playlist = if (playlistName != null) playlistDAO.getPlaylistForUserByName(
            userId,
            playlistName
        ) else playlistDAO.getPlaylistForUserById(userId, playlistId)

        // If no playlist found then return
        // TODO: Custom exception classes for this stuff.
        if (playlist == null) {
            logger.log(Level.WARNING, "No playlist found for id: $playlistId or name: $playlistName")
            commandEvent.reply(commandEvent.client.warning + " Playlist not found! Please check the id/name.")
            return
        }

        val conn = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        val loadingMessage = commandEvent.channel.sendMessage("Starting load of playlist ${playlist.name}").complete()
        // Queue up the tracks
        conn.queuePlaylist(playlist.getTracks()!!.map { it.url!! }.toList(), commandEvent, loadingMessage)
    }
}