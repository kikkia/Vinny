package com.bot.commands

import com.bot.db.PlaylistDAO
import com.bot.exceptions.newstyle.UserVisibleException
import com.bot.i18n.Translator
import com.bot.voice.GuildVoiceProvider
import com.bot.commands.control.MenuSelectControlEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class SelectMenuInteractionHandler: ListenerAdapter() {

    private val playlistDAO = PlaylistDAO.getInstance()
    private val guildVoiceProvider = GuildVoiceProvider.getInstance()
    private val translator = Translator.getInstance()

    // Since these menus are only created by direct commands and are one use we dont need to do so much metrics or other
    // similar stuff as in the button commands.
    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        val parsedId = event.selectMenu.id!!.split("-")
        val action = parsedId.first()

        when (action) {
            "loadplaylist" -> handlePlaylistLoad(event)
        }

        super.onStringSelectInteraction(event)
    }

    private fun handlePlaylistLoad(event: StringSelectInteractionEvent) {
        val conn = guildVoiceProvider.getGuildVoiceConnection(event.guild!!)
        val playlist = playlistDAO.getPlaylistById(event.values[0])
        if (playlist != null) {
            val loadingMessage = event.channel.sendMessage("Starting load of playlist ${playlist.name}").complete()
            try {
                conn.queuePlaylist(
                    playlist.getTracks()!!.map { it.url!! }.toList(),
                    MenuSelectControlEvent(event),
                    loadingMessage
                )
                event.hook.editOriginal("Loading playlist...").queue()
            } catch (e: UserVisibleException) {
                event.hook.editOriginal(translator.translate(e.outputId, event.userLocale.locale))
            }
        }
    }
}