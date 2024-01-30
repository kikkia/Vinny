package com.bot.commands.owner

import com.bot.commands.OwnerCommand
import com.bot.db.ResumeAudioDAO
import com.bot.db.mappers.ResumeAudioTrackMapper
import com.bot.voice.GuildVoiceProvider
import com.jagrosh.jdautilities.command.CommandEvent

class TestRebootAnnounceCommand : OwnerCommand() {
    val resumeAudioDAO = ResumeAudioDAO.getInstance()
    init {
        name = "testreboot"
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
                    logger.severe("Failed to store guild ${conn.guild.id}", e)
                    commandEvent.replyError("Failed to store for ${conn.guild}, $e")
                }
            }
        }
        commandEvent.reactSuccess()
    }
}