package com.bot.tasks;

import com.bot.db.MembershipDAO;
import com.bot.utils.Logger;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;

public class LeaveGuildDeferredTask extends Thread {

    private final MembershipDAO membershipDAO;
    private GuildLeaveEvent guildLeaveEvent;
    private Logger logger;

    public LeaveGuildDeferredTask(GuildLeaveEvent guildLeaveEvent) {
        this.guildLeaveEvent = guildLeaveEvent;
        this.membershipDAO = MembershipDAO.getInstance();
        this.logger = new Logger(this.getClass().getName());
    }

    @Override
    public void run() {
        for (Member m : guildLeaveEvent.getGuild().getMembers()) {
            membershipDAO.removeUserMembershipToGuild(m.getUser().getId(), guildLeaveEvent.getGuild().getId());
        }

        logger.info("Left guild: " + guildLeaveEvent.getGuild().getName() + " with " + guildLeaveEvent.getGuild().getMembers().size() + " members");
    }
}
