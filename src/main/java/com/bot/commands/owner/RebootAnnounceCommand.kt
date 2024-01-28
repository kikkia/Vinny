package com.bot.commands.owner

import com.bot.commands.OwnerCommand
import com.bot.db.ResumeAudioDAO
import com.bot.db.mappers.ResumeAudioTrackMapper
import com.bot.voice.GuildVoiceProvider
import com.jagrosh.jdautilities.command.CommandEvent

class RebootAnnounceCommand : OwnerCommand() {
    val resumeAudioDAO = ResumeAudioDAO.getInstance()
    init {
        name = "reboot"
    }

    override fun executeCommand(commandEvent: CommandEvent) {
        val connections = GuildVoiceProvider.getInstance().getAll()
        for (conn in connections) {
            if (conn.nowPlaying() != null) {
                try {
                    resumeAudioDAO.storeResumeGuild(conn.guild.id,
                        conn.currentVoiceChannel!!.id,
                        conn.lastTextChannel!!.id,
                        conn.getVolume(),
                        conn.volumeLocked,
                        ResumeAudioTrackMapper.queueToTracks(conn))
                } catch (e: Exception) {
                    commandEvent.replyError("Failed to store for ${conn.guild}, $e")
                }
                conn.lastTextChannel!!.sendMessage("Vinny is rebooting for maintenance. I saved your queue and position in the current track. I will resume when I am done updating. If you want to follow the latest updates for Vinny, join the support server with the `~support` command. Sorry for the inconvenience.").complete()
            }
        }
        commandEvent.reactSuccess()
    }
}