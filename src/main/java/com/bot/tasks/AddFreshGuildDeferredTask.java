package com.bot.tasks;

import com.bot.db.ChannelDAO;
import com.bot.db.GuildDAO;
import com.bot.db.MembershipDAO;
import com.bot.utils.GuildUtils;
import com.bot.utils.Logger;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;

/**
 * Deferred task to delegate adding new guilds to the db and send a welcome message on a separate thread.
 */
public class AddFreshGuildDeferredTask extends Thread {

    private final MembershipDAO membershipDAO;
    private final ChannelDAO channelDAO;
    private final GuildDAO guildDAO;
    private GuildJoinEvent joinEvent;
    private Logger logger;

    public AddFreshGuildDeferredTask(GuildJoinEvent joinEvent) {
        this.joinEvent = joinEvent;
        this.logger = new Logger(AddFreshGuildDeferredTask.class.getName());
        this.guildDAO = GuildDAO.getInstance();
        this.membershipDAO = MembershipDAO.getInstance();
        this.channelDAO = ChannelDAO.getInstance();
    }

    @Override
    public void run() {
        logger.info("Joining guild: " + joinEvent.getGuild().getName() + " with " + joinEvent.getGuild().getMembers().size() + " members");

        try {
            GuildUtils.sendWelcomeMessage(joinEvent);
        } catch (Exception e) {
            logger.severe("Failed to send guild welcome message to: " + joinEvent.getGuild().getId(), e);
        }

        Guild guild = joinEvent.getGuild();
        guildDAO.addGuild(guild);

        for (Member m : guild.getMembers()) {
            membershipDAO.addUserToGuild(m.getUser(), guild);
        }
        for (TextChannel t : guild.getTextChannels()) {
            channelDAO.addTextChannel(t);
        }
        for (VoiceChannel v : guild.getVoiceChannels()) {
            channelDAO.addVoiceChannel(v);
        }

        logger.info("Completed addition of fresh guild. " + guild.getId());
    }
}
