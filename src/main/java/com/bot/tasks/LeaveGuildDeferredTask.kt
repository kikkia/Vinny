package com.bot.tasks

import com.bot.db.MembershipDAO
import com.bot.utils.Logger
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent

class LeaveGuildDeferredTask(private val guildLeaveEvent: GuildLeaveEvent) : Thread() {

    private val membershipDAO: MembershipDAO = MembershipDAO.getInstance()
    private val logger: Logger = Logger(this.javaClass.name)

    override fun run() {
        for (m in guildLeaveEvent.guild.members) {
            membershipDAO.removeUserMembershipToGuild(m.user.id, guildLeaveEvent.guild.id)
        }

        logger.info("Left guild: " + guildLeaveEvent.guild.name + " with " + guildLeaveEvent.guild.members.size + " members")
    }
}
