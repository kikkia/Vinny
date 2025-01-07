package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.PlaylistDAO
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

class LoadPlaylistSlashCommand: VoiceSlashCommand() {
    
    private val playlistDAO = PlaylistDAO.getInstance()

    init {
        this.name = "load-playlist"
        this.help = "Select and play a saved playlist"
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val playlists = playlistDAO.getPlaylistsForGuild(command.guild!!.id)
        playlists.addAll(playlistDAO.getPlaylistsForUser(command.user.id))
        if (playlists.isEmpty()) {
            command.replyWarningTranslated("NO_SAVED_PLAYLISTS_FOUND")
            return
        }
        val selectBuilder = StringSelectMenu.create("loadplaylist-" + command.user.id)
        for (p in playlists) {
            val desc = if (p.ownerID == command.user.id)
                translator.translate("YOUR_PLAYLIST", command.userLocale.locale, p.getTracks()!!.size.toString())
            else
                translator.translate("GUILD_PLAYLIST", command.userLocale.locale, p.getTracks()!!.size.toString())

            selectBuilder.addOption(p.name!!, p.id.toString(), desc)
        }
        command.replyTranslatedWithActionBar("CHOOSE_PLAYLIST", mutableListOf(selectBuilder.build()), true)
    }
}