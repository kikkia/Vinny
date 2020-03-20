package com.bot.utils;

import com.bot.db.ChannelDAO;
import com.bot.db.GuildDAO;
import com.bot.db.MembershipDAO;
import com.bot.exceptions.ForbiddenCommandException;
import com.bot.exceptions.PermsOutOfSyncException;
import com.bot.models.InternalGuild;
import com.bot.models.InternalGuildMembership;
import com.bot.models.InternalTextChannel;
import com.bot.models.InternalVoiceChannel;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Role;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class CommandPermissions {
    private static Logger LOGGER = new Logger("Command Permissions");

    private static ChannelDAO channelDAO = ChannelDAO.getInstance();
    private static MembershipDAO membershipDAO = MembershipDAO.getInstance();
    private static GuildDAO guildDAO = GuildDAO.getInstance();


    public static boolean canExecuteCommand(Command command, CommandEvent commandEvent) throws ForbiddenCommandException, PermsOutOfSyncException {
        return canExecuteCommand(command.getCategory(), commandEvent);
    }

    public static boolean canExecuteCommand(Command.Category commandCategory, CommandEvent commandEvent) throws ForbiddenCommandException, PermsOutOfSyncException {
        // If its a PM then screw permissions
        if (commandEvent.isFromType(ChannelType.PRIVATE))
            return true;

        if (commandCategory.equals(CommandCategories.OWNER)) {
            Config config = Config.getInstance();
            return commandEvent.getAuthor().getId().equals(config.getConfig(Config.OWNER_ID));
        }

        InternalGuild guild;

        try {
            guild = guildDAO.getGuildById(commandEvent.getGuild().getId());
            if (guild == null) { // Membership is missing
                throw new SQLException("Guild is missing");
            }
        }  catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get guild from db: " + commandEvent.getGuild().getId() + " attempting to add", e);
            // Throw an exception and try to add the guild
            guildDAO.addFreshGuild(commandEvent.getGuild());
            throw new ForbiddenCommandException("There is a problem with this guild in the db. " +
                    "I will attempt to fix it, please try again later. If this issue persists please contact the devs on the support server.");
        }

        // Check the roles returned
        Role requiredRole = commandEvent.getGuild().getRoleById(guild.getRequiredPermission(commandCategory));

        // We check owner as well, because if they are the owner they get around this check
        if (requiredRole == null && !commandEvent.getMember().isOwner()) {
            throw new PermsOutOfSyncException("Role required for permission not found.");
        }

        // Get users role, if they have none then use default
        List<Role> roleList;
        Role highestRole;
        if (commandEvent.getMember() == null) {
            highestRole = commandEvent.getGuild().getPublicRole();
        } else {
            roleList = commandEvent.getMember().getRoles();
            if (roleList.isEmpty()) {
                highestRole = commandEvent.getGuild().getPublicRole();
            } else {
                highestRole = commandEvent.getMember().getRoles().get(0);
            }
        }

        if (!commandEvent.getMember().isOwner() && highestRole.getPosition() < requiredRole.getPosition()) {
            throw new ForbiddenCommandException("You do not have the required role to use this command. You must have at least the " +
                    requiredRole.getName() + " role or higher to use " + commandCategory.getName() + " commands.");
        }

        // Checking Membership permissions
        InternalGuildMembership membership;
        try {
            membership = membershipDAO.getUserMembershipByIdInGuild(commandEvent.getAuthor().getId(), commandEvent.getGuild().getId());
            if (membership == null) { // Membership is missing
                throw new SQLException("Membership is missing");
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to get membership from db when checking command perms. ", e);
            membershipDAO.addUserToGuild(commandEvent.getAuthor(), commandEvent.getGuild());
            throw new ForbiddenCommandException("There is a problem with your association to the guild in the db. This can happen right after adding me to your server. " +
                    "I will attempt to fix it, please try again. If this issue persists please contact the devs on the support server.");
        }

        if (!membership.canUseBot()) {
            throw new ForbiddenCommandException("Your ability to use commands has been disabled. " +
                    "To unlock commands please talk to a guild admin.");
        }

        // TODO: Check channel permissions
        if (commandCategory == CommandCategories.VOICE) {
            // If their in a voice channel the doesn't allow voice, then dont let them use it
            if (commandEvent.getMember().getVoiceState().inVoiceChannel()) {
                InternalVoiceChannel voiceChannel;
                // Get voice channel, if not present add it
                try {
                    voiceChannel = channelDAO.getVoiceChannelForId(
                            commandEvent.getMember().getVoiceState().getChannel().getId());
                    if (voiceChannel == null) {
                        throw new SQLException("Voice channel is missing");
                    }
                } catch (SQLException e) {
                    guildDAO.addGuild(commandEvent.getGuild());
                    LOGGER.severe("Failed to get voice channel: ", e);
                    channelDAO.addVoiceChannel(commandEvent.getMember().getVoiceState().getChannel());
                    throw new ForbiddenCommandException("There is a problem with the voice channel in the db. " +
                            "I will attempt to fix it, please try again later. If this issue persists please contact the devs on the support server.");
                }

                if (!voiceChannel.isVoiceEnabled()) {
                    throw new ForbiddenCommandException("This voice channel has commands disabled. Please contact" +
                            " a mod in your server to enable them.");
                }
            } else {
                throw new ForbiddenCommandException("You must be in a voice channel to use a voice command");
            }
        }

        InternalTextChannel textChannel;
        // Try to get the text channel and check, if its none, add it
        try {
            textChannel = channelDAO.getTextChannelForId(commandEvent.getTextChannel().getId(), true);
            if (textChannel == null) {
                throw new SQLException("Text channel is missing");
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to get text channel: ", e);
            channelDAO.addTextChannel(commandEvent.getTextChannel());
            throw new ForbiddenCommandException("There is a problem with the text channel in the db. " +
                    "I will attempt to fix it, please try again later. If this issue persists please contact the devs on the support server.");
        }

        if (commandCategory == CommandCategories.NSFW && !commandEvent.getTextChannel().isNSFW()) {
            throw new ForbiddenCommandException("This channel is not marked in discord as nsfw. " +
                    "To enable it, please go into the channel settings in discord and enable nsfw.");
        }

        return textChannel.isCommandsEnabled();
    }
}
