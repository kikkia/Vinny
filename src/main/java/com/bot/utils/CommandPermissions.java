package com.bot.utils;

import com.bot.db.ChannelDAO;
import com.bot.db.GuildDAO;
import com.bot.db.MembershipDAO;
import com.bot.exceptions.ForbiddenCommandException;
import com.bot.exceptions.PermsOutOfSyncException;
import com.bot.models.InternalGuild;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class CommandPermissions {
    private static final Logger LOGGER = new Logger("Command Permissions");

    private static final ChannelDAO channelDAO = ChannelDAO.getInstance();
    private static final MembershipDAO membershipDAO = MembershipDAO.getInstance();
    private static final GuildDAO guildDAO = GuildDAO.getInstance();


    public static boolean canExecuteCommand(Command command, CommandEvent commandEvent) throws ForbiddenCommandException, PermsOutOfSyncException {
        return canExecuteCommand(command.getCategory(), commandEvent);
    }

    public static boolean canExecuteCommand(Command.Category commandCategory, CommandEvent commandEvent) throws ForbiddenCommandException, PermsOutOfSyncException {
        // If its a PM then screw permissions
        if (commandEvent.isFromType(ChannelType.PRIVATE))
            return true;

        if (commandCategory.equals(CommandCategories.OWNER)) {
            VinnyConfig config = VinnyConfig.Companion.instance();
            return commandEvent.getAuthor().getId().equals(config.getDiscordConfig().getOwnerId());
        }

        if (commandCategory == CommandCategories.NSFW && !commandEvent.getTextChannel().isNSFW()) {
            throw new ForbiddenCommandException("This channel is not marked in discord as nsfw. " +
                    "To enable it, please go into the channel settings in discord and enable nsfw.");
        }

        if (commandEvent.getGuild().getOwnerId().equals(commandEvent.getAuthor().getId())) {
            return true;
        }

        InternalGuild guild;
        try {
            guild = ScheduledCommandUtils.isScheduled(commandEvent) ?
                    guildDAO.getGuildById(commandEvent.getGuild().getId()) :
                    guildDAO.getGuildById(commandEvent.getGuild().getId(), false);
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
        Role requiredBaseRole = commandEvent.getGuild().getRoleById(guild.getRequiredPermission(CommandCategories.GENERAL));

        // Get users role, if they have none then use default
        List<Role> roleList;
        Role highestRole;
        boolean isOwner;

        // For scheduled commands, if member left then just get @everyone perms
        if (commandEvent.getMember() == null) {
            highestRole = commandEvent.getGuild().getPublicRole();
            isOwner = false;
        } else {
            roleList = commandEvent.getMember().getRoles();
            if (roleList.isEmpty()) {
                highestRole = commandEvent.getGuild().getPublicRole();
            } else {
                highestRole = commandEvent.getMember().getRoles().get(0);
            }
            isOwner = commandEvent.getMember().isOwner();
        }


        // We check owner as well, because if they are the owner they get around this check
        if (requiredRole == null && !isOwner) {
            throw new PermsOutOfSyncException("Role required for permission not found.");
        } else if (requiredBaseRole == null && !isOwner) {
            throw new PermsOutOfSyncException("Base role not found! Was it deleted?");
        }


        if (!isOwner && highestRole.getPosition() < requiredBaseRole.getPosition()) {
            throw new ForbiddenCommandException("You do not have the required base role to run any commands. You must have at least the `" +
                    requiredBaseRole.getName() + "` role or higher to use any Vinny commands.");
        }

        if (!isOwner && highestRole.getPosition() < requiredRole.getPosition()) {
            throw new ForbiddenCommandException("You do not have the required role to use this command. You must have at least the `" +
                    requiredRole.getName() + "` role or higher to use " + commandCategory.getName() + " commands.");
        }

        if (commandCategory == CommandCategories.VOICE) {

            if (VinnyConfig.Companion.instance().getVoiceConfig().getDefaultSearchProvider().equalsIgnoreCase("disabled")) {
                throw new ForbiddenCommandException("Currently Vinny's voice service is under maintenance until further " +
                        "notice. For more information see the support server. That is where " +
                        "updates will be posted. Thank you for understanding.");
            }

            // If their in a voice channel the doesn't allow voice, then dont let them use it
            if (commandEvent.getMember().getVoiceState() == null || !commandEvent.getMember().getVoiceState().inAudioChannel()) {
                throw new ForbiddenCommandException("You must be in a voice channel to use a voice command");
            }
        }

        return true;
    }
}
