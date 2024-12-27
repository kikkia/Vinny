package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.PlaylistDAO
import com.bot.models.Playlist
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator
import java.util.stream.Collectors

class ListPlaylistsSlashCommand(private val waiter: EventWaiter): VoiceSlashCommand() {
    private val playlistDAO = PlaylistDAO.getInstance()
    val builder: ButtonEmbedPaginator.Builder = ButtonEmbedPaginator.Builder()

    init {
        this.name = "playlists"
        this.help = "Lists all playlists you can use"
        this.builder.setEventWaiter(waiter)
        this.builder.wrapPageEnds(true)
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val playlists = playlistDAO.getPlaylistsForGuild(command.guild!!.id)
        playlists.addAll(playlistDAO.getPlaylistsForUser(command.user.id))

        if (playlists.isEmpty()) {
            command.replyWarning("VOICE_PLAYLISTS_NONE_FOUND")
            return
        }

        val list = playlists.stream().map { obj: Playlist ->
            val scope = if (obj.ownerID == command.user.id) "Your" else "Guild"
            "$scope $obj"
        }.collect(Collectors.toList())
        builder.setItems(*list.toTypedArray())
        builder.setText("Playlists available")
        builder.build().paginate(command.hook, 1)
    }
}