package com.bot.tasks

import com.bot.db.ResumeAudioDAO
import com.bot.utils.Logger
import com.bot.voice.GuildVoiceProvider
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.ReadyEvent
import java.util.*

class ResumeAudioTask(private val readyEvent: ReadyEvent) : Thread() {
    private val logger = Logger(this.javaClass.simpleName)
    private val resumeAudioDAO = ResumeAudioDAO.getInstance()
    private val guildVoiceProvider = GuildVoiceProvider.getInstance()

    override fun run() {
        val jda = readyEvent.jda
        try {
            // Get all guilds to resume, and check if they exist on this shard. If so, then we can try to resume them
            val resumeGuildIds = resumeAudioDAO.getAllResumeGuilds()
            val toResumeGuilds = LinkedList<Guild>()
            for (guildId in resumeGuildIds) {
                val guild = jda.getGuildById(guildId)
                if (guild != null) {
                    toResumeGuilds.add(guild)
                }
            }
            logger.info("Shard ${jda.shardInfo.shardId} starting resume audio of ${toResumeGuilds.size} guilds.")
            for (guild in toResumeGuilds) {
                try {
                    val resumeSetup = resumeAudioDAO.getResumeGuild(guild.id)
                    val connection = guildVoiceProvider.getGuildVoiceConnection(guild)
                    connection.lastTextChannel = guild.getTextChannelById(resumeSetup.textChannelId)
                    connection.currentVoiceChannel = guild.getVoiceChannelById(resumeSetup.voiceChannelId)
                    if (connection.currentVoiceChannel == null || connection.lastTextChannel == null) {
                        logger.warning("Voice or text channel not found when rebooting voice failing guild: ${guild.id}")
                        return
                    }
                    connection.resumeAudioAfterReboot(resumeSetup)
                    resumeAudioDAO.deleteAllForGuildId(guild.id)
                } catch (e: Exception) {
                    logger.severe("Failed to restart audio for guild: ${guild.id}", e)
                }
            }
            logger.info("Shard ${jda.shardInfo.shardId} finished with resume audio task")
        } catch (e: Exception) {
            logger.severe("Failed to restart audio for shard: ${jda.shardInfo.shardId}", e)
        }
    }
}