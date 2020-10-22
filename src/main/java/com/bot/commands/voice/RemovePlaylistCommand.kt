package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.bot.db.PlaylistDAO
import com.bot.models.Playlist
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission

class RemovePlaylistCommand : VoiceCommand() {
    private val playlistDAO = PlaylistDAO.getInstance()

    override fun executeCommand(commandEvent: CommandEvent) {
        if (commandEvent.args.isEmpty()) {
            commandEvent.replyWarning("You need to provide the ID or the name of the playlist")
        }

        var playlist: Playlist?
        try {
            playlist = playlistDAO.getPlaylistForUserById(commandEvent.author.id, Integer.parseInt(commandEvent.args))

            if (playlist == null) {
                playlist = playlistDAO.getPlaylistForGuildById(commandEvent.guild.id, Integer.parseInt(commandEvent.args))
            }
        } catch (e : NumberFormatException) {
            playlist = playlistDAO.getPlaylistForUserByName(commandEvent.author.id, commandEvent.args)

            if (playlist == null) {
                playlist = playlistDAO.getPlaylistForGuildByName(commandEvent.guild.id, commandEvent.args)
            }
        }

        if (playlist == null) {
            commandEvent.replyWarning("I could not find that playlist")
            return
        }
        if (playlist.ownerID == commandEvent.guild.id) {
            if (!commandEvent.member.hasPermission(Permission.MANAGE_SERVER)) {
                commandEvent.replyWarning("You need the MANAGE SERVER permission to remove guild playlists")
                return
            }
        }

        playlistDAO.removePlaylist(playlist)
        commandEvent.reactSuccess()
    }

    init {
        name = "removeplaylist"
    }
}