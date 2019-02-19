package com.bot.utils;

import com.bot.db.ChannelDAO;
import com.bot.db.GuildDAO;
import com.bot.db.MembershipDAO;
import com.bot.models.InternalGuild;
import com.bot.models.InternalGuildMembership;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Role;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandPermissions {
    private static Logger LOGGER = Logger.getLogger("Command Permissions");

    private static ChannelDAO channelDAO = ChannelDAO.getInstance();
    private static MembershipDAO membershipDAO = MembershipDAO.getInstance();
    private static GuildDAO guildDAO = GuildDAO.getInstance();


    public static boolean canExecuteCommand(Command command, CommandEvent commandEvent) {
        InternalGuild guild;

        try {
            guild = guildDAO.getGuildById(commandEvent.getGuild().getId());
            if (guild == null) { // Membership is missing
                throw new SQLException("Guild is missing");
            }
        }  catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get guild from db: " + commandEvent.getGuild().getId() + " attempting to add", e.getMessage());
            // Alert the user that there is an issue and that we will need to start an emergency sync
            commandEvent.reply(commandEvent.getClient().getError() + " There is a problem with this guild in the db. " +
                    "I will attempt to fix it, please try again later. If this issue persists please contact the devs on the support server.");
            guildDAO.addFreshGuild(commandEvent.getGuild());
            return false;
        }

        // Check the roles returned
        Role requiredRole = commandEvent.getGuild().getRoleById(guild.getRequiredPermission(command.getCategory()));
        Role highestRole = commandEvent.getMember().getRoles().get(0);
        if (highestRole.getPosition() < requiredRole.getPosition()) {
            // Reply to the command event stating that they do not hold the position required.
            commandEvent.reply(commandEvent.getClient().getError() +
                    "Error: You do not have the required role to use this command. You must have at least the " +
                    requiredRole.getName() + " role or higher to use " + command.getCategory().getName() + " commands.");
            return false;
        }

        // Checking Membership permissions
        InternalGuildMembership membership;
        try {
            membership = membershipDAO.getUserMembershipByIdInGuild(commandEvent.getAuthor().getId(), commandEvent.getGuild().getId());
            if (membership == null) { // Membership is missing
                throw new SQLException("Membership is missing");
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to get membership from db when checking command perms. " + e.getMessage());
            commandEvent.reply(commandEvent.getClient().getError() + " There is a problem with you association to the guild in the db. " +
                    "I will attempt to fix it, please try again later. If this issue persists please contact the devs on the support server.");
            membershipDAO.addUserToGuild(commandEvent.getAuthor(), commandEvent.getGuild());
            return false;
        }

        if (!membership.canUseBot()) {
            commandEvent.reply(commandEvent.getClient().getError() + " Your ability to use commands has been disabled. " +
                    "To unlock commands please talk to a guild admin.");
            return false;
        }

        // TODO: Check channel permissions
        

        return true;
    }
}
