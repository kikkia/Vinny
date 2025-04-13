package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.db.PlaylistDAO
import com.bot.voice.BaseAudioTrack
import com.bot.voice.QueuedAudioTrack
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.util.*

class SavePlaylistSlashCommand: VoiceSlashCommand() {

    private val playlistDAO = PlaylistDAO.getInstance()

    init {
        this.name = "save-playlist"
        this.help = "Saves the current queue of songs as a playlist"
        this.options = listOf(OptionData(OptionType.STRING, "name", "The name of the playlist", true),
                OptionData(OptionType.BOOLEAN, "guild", "If true allows all users in the guild to use it.", true))
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val name = command.optString("name")
        val guildScope = command.optBoolean("guild")

        val conn = provider.getGuildVoiceConnection(command.guild!!)

        val tracks = conn.getQueuedTracks()
        val nowPlaying = conn.nowPlaying()
        val trackList: MutableList<BaseAudioTrack> = LinkedList()

        if (nowPlaying == null) {
            command.replyWarningTranslated("VOICE_NO_PLAYING_TRACK")
            return
        }

        trackList.add(nowPlaying)
        trackList.addAll(tracks)

        if (guildScope) {
            playlistDAO.createPlaylistForGuild(command.guild!!.id, name, trackList)
        } else {
            playlistDAO.createPlaylistForUser(command.user.id, name, trackList)
        }
        command.replySuccessTranslated("VOICE_PLAYLIST_CREATE")
    }
}