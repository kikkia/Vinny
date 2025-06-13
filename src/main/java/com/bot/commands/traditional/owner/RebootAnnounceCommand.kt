package com.bot.commands.traditional.owner

import com.bot.commands.traditional.OwnerCommand
import com.bot.db.ResumeAudioDAO
import com.bot.db.mappers.ResumeAudioTrackMapper
import com.bot.voice.GuildVoiceProvider
import com.jagrosh.jdautilities.command.CommandEvent
import java.lang.Exception

class RebootAnnounceCommand : OwnerCommand() {
    private val resumeAudioDAO = ResumeAudioDAO.getInstance()

    init {
        name = "reboot"
    }

    override fun executeCommand(commandEvent: CommandEvent) {
        // Just pre-emptively remove all leftovers
        resumeAudioDAO.removeAll()
        val connections = GuildVoiceProvider.getInstance().getAll()
        for (conn in connections) {
            if (conn.nowPlaying() != null) {
                val locale = conn.guild.locale.locale
                try {
                    resumeAudioDAO.storeResumeGuild(conn.guild.id,
                        conn.guild.selfMember.voiceState!!.channel!!.id,
                        conn.lastTextChannel!!.id,
                        conn.getVolume(),
                        conn.volumeLocked,
                        conn.oauthConfig?.userId,
                        ResumeAudioTrackMapper.queueToTracks(conn))
                } catch (e: Exception) {
                    logger.severe("Failed to store guild ${conn.guild.id}", e)
                    commandEvent.replyError("Failed to store for ${conn.guild}, $e")
                    e.printStackTrace()
                    conn.lastTextChannel!!.sendMessage(translator.translate("REBOOT_ERROR_MESSAGE", locale)).complete()
                    continue
                }
                // This shouldnt fail but if it does it is likely do to a perm change or deleted channel or smth.
                try {
                    conn.lastTextChannel!!.sendMessage(translator.translate("REBOOT_ANNOUNCE_MESSAGE", locale)).complete()
                } catch (e: Exception) {
                    logger.severe("Failed to post reboot message", e)
                }
            }
        }
        commandEvent.reactSuccess()
    }
}