package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.PlaylistDAO
import com.bot.models.Playlist
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class DeletePlaylistSlashCommand: VoiceSlashCommand(false) {

    private val playlistDAO = PlaylistDAO.getInstance()

    init {
        this.name = "delete-playlist"
        this.help = "Deletes a selected playlist"
        this.options = listOf(OptionData(OptionType.INTEGER, "name", "The playlist to delete", true, true))
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val id = command.optLong("name")
        val playlists = playlistDAO.getPlaylistsForUser(command.user.id)
        val toRemove = playlists.firstOrNull { it.id == id.toInt() }

        if (toRemove == null) {
            command.replyWarningTranslated("VOICE_PLAYLIST_NOT_FOUND")
            return
        }

        playlistDAO.removePlaylist(toRemove)

        command.replySuccessTranslated("VOICE_PLAYLIST_DELETED")
    }

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent?) {
        val choices: List<Choice>?

        if (event!!.focusedOption.name == "name") {
            val playlists = playlistDAO.getPlaylistsForUser(event.user.id)

            event.replyChoices(playlists.map { Choice("${it.name!!} - ${it.getTracks()!!.size} tracks", it.id.toLong())}).queue()
        }
        super.onAutoComplete(event)
    }
}