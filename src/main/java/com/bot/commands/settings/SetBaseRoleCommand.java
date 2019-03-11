package com.bot.commands.settings;

import com.bot.Bot;
import com.bot.commands.ModerationCommand;
import com.bot.db.GuildDAO;
import com.bot.models.InternalGuild;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SetBaseRoleCommand extends ModerationCommand {

    private GuildDAO guildDAO;
    private static Logger LOGGER = Logger.getLogger(SetBaseRoleCommand.class.getName());

    public SetBaseRoleCommand() {
        this.name = "baserole";
        this.help = "Sets the minimum role required to use any command. (Mod command permission required)";
        this.arguments = "<Role mention>";
        this.guildDAO = GuildDAO.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        InternalGuild guild = null;
        Guild commandGuild = commandEvent.getGuild();

        try {
            guild = guildDAO.getGuildById(commandGuild.getId());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Problem getting guild settings " + e.getMessage());
            commandEvent.reply(commandEvent.getClient().getError() + " There was a problem getting the settings for your guild. Please contact the developer on the support server. " + Bot.SUPPORT_INVITE_LINK);
            return;
        }

        if (guild == null) {
            LOGGER.log(Level.WARNING, "Guild not found in db, attempting to add: " + commandGuild.getId());
            commandEvent.reply(commandEvent.getClient().getWarning() + " This guild was not found in my database. I am going to try to add it. Please standby.");

            if (!guildDAO.addGuild(commandGuild)) {
                LOGGER.log(Level.SEVERE, "Failed to add the guild to the db");
                commandEvent.reply(commandEvent.getClient().getError() + " Error adding the guild to the db. Please contact the developer on the support server." + Bot.SUPPORT_INVITE_LINK);
                return;
            }
            commandEvent.reply(commandEvent.getClient().getSuccess() + " Added the guild to the database. Retrying");
            execute(commandEvent);
            return;
        }

        List<Role> mentionedRoles = commandEvent.getMessage().getMentionedRoles();
        if (mentionedRoles == null || mentionedRoles.isEmpty()) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " You must specify a role.");
            return;
        }

        // Just use the first mentioned roles
        if (!guildDAO.updateMinBaseRole(guild.getId(), mentionedRoles.get(0).getId())) {
            LOGGER.log(Level.SEVERE, "Failed to update base role for guild " + guild.getId());
            commandEvent.reply("Something went wrong. Please contact the developers on the support server. " + Bot.SUPPORT_INVITE_LINK);
        }
        commandEvent.getMessage().addReaction(commandEvent.getClient().getSuccess()).queue();

    }
}
