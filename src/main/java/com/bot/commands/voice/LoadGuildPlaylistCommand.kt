package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.bot.db.PlaylistDAO
import com.bot.models.Playlist
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import java.util.logging.Level

class LoadGuildPlaylistCommand : VoiceCommand() {
    private val playlistDAO: PlaylistDAO

    init {
        name = "loadgplaylist"
        arguments = "<playlist id|playlist name>"
        help = "Loads one of the guilds playlists. You must either specify the id or the name of the playlist."
        playlistDAO = PlaylistDAO.getInstance()
    }

    @Trace(operationName = "executeCommand", resourceName = "LoadGuildPlaylist")
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
        val guildId = commandEvent.guild.id
        playlist = if (playlistName != null) playlistDAO.getPlaylistForGuildByName(
            guildId,
            playlistName
        ) else playlistDAO.getPlaylistForGuildById(guildId, playlistId)

        // If no playlist found then return
        // TODO: Custom exception classes for this stuff.
        if (playlist == null) {
            logger.log(
                Level.WARNING,
                "No playlist found for id: " + playlistId + " or name: " + playlistName + "for guild: " + guildId
            )
            commandEvent.reply(commandEvent.client.warning + " Playlist not found! Please check the id/name.")
            return
        }

        val conn = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        val loadingMessage = commandEvent.textChannel.sendMessage("Starting load of playlist ${playlist.name}").complete()
        // Queue up the tracks
        conn.queuePlaylist(playlist.getTracks()!!.map { it.url!! }.toList(), commandEvent, loadingMessage)
    }
}