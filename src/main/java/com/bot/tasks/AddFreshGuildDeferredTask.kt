package com.bot.tasks

import com.bot.db.ChannelDAO
import com.bot.db.GuildDAO
import com.bot.db.MembershipDAO
import com.bot.utils.GuildUtils
import com.bot.utils.Logger
import net.dv8tion.jda.api.events.guild.GuildJoinEvent

/**
 * Deferred task to delegate adding new guilds to the db and send a welcome message on a separate thread.
 */
class AddFreshGuildDeferredTask(private val joinEvent: GuildJoinEvent) : Thread() {

    private val membershipDAO: MembershipDAO = MembershipDAO.getInstance()
    private val channelDAO: ChannelDAO = ChannelDAO.getInstance()
    private val guildDAO: GuildDAO = GuildDAO.getInstance()
    private val logger: Logger = Logger(AddFreshGuildDeferredTask::class.java.name)

    override fun run() {
        logger.info("Joining guild: " + joinEvent.guild.name + " with " + joinEvent.guild.members.size + " members")

        try {
            GuildUtils.sendWelcomeMessage(joinEvent)
        } catch (e: Exception) {
            logger.severe("Failed to send guild welcome message to: " + joinEvent.guild.id, e)
        }

        val guild = joinEvent.guild
        guildDAO.addGuild(guild)

        for (m in guild.members) {
            membershipDAO.addUserToGuild(m.user, guild)
        }
        for (t in guild.textChannels) {
            channelDAO.addTextChannel(t)
        }
        for (v in guild.voiceChannels) {
            channelDAO.addVoiceChannel(v)
        }

        logger.info("Completed addition of fresh guild. " + guild.id)
    }
}
