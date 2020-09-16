package com.bot.tasks

import com.bot.db.GuildDAO
import com.bot.db.MembershipDAO
import com.bot.db.RssDAO
import com.bot.db.ScheduledCommandDAO
import com.bot.utils.Logger
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent

class LeaveGuildDeferredTask(private val guildLeaveEvent: GuildLeaveEvent) : Thread() {

    private val membershipDAO: MembershipDAO = MembershipDAO.getInstance()
    private val guildDAO: GuildDAO = GuildDAO.getInstance()
    private val scheduledDAO: ScheduledCommandDAO = ScheduledCommandDAO.getInstance()
    private val rssDAO = RssDAO.getInstance()
    private val logger: Logger = Logger(this.javaClass.name)

    override fun run() {
        for (m in guildLeaveEvent.guild.members) {
            membershipDAO.removeUserMembershipToGuild(m.user.id, guildLeaveEvent.guild.id)
        }

        guildDAO.setGuildActive(guildLeaveEvent.guild.id, false)
        scheduledDAO.removeAllScheduledInGuild(guildLeaveEvent.guild.id)
        rssDAO.removeAllSubsInGuild(guildLeaveEvent.guild.id)

        logger.info("Left guild: " + guildLeaveEvent.guild.name + " with " + guildLeaveEvent.guild.members.size + " members")
    }
}
