package com.bot.commands.settings;

import com.bot.Bot;
import com.bot.commands.ModerationCommand;
import com.bot.db.GuildDAO;
import com.bot.models.InternalGuild;
import com.bot.utils.CommandPermissions;
import com.bot.utils.Logger;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;

import java.util.List;
import java.util.logging.Level;

public class SetNSFWCommand extends ModerationCommand {
    private static Logger LOGGER = new Logger(SetNSFWCommand.class.getName());

    private GuildDAO guildDAO;

    public SetNSFWCommand() {
        this.name = "nsfwrole";
        this.help = "Sets the minimum required to use an NSFW command. (Mod command permission required)";
        this.arguments = "<Role mention>";
        this.guildDAO = GuildDAO.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
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
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
            return;
        }

        if (guild == null) {
            LOGGER.log(Level.WARNING, "Guild not found in db, attempting to add: " + commandGuild.getId());
            commandEvent.reply(commandEvent.getClient().getWarning() + " This guild was not found in my database. I am going to try to add it. Please standby.");

            if (!guildDAO.addGuild(commandGuild)) {
                LOGGER.log(Level.SEVERE, "Failed to add the guild to the db");
                commandEvent.reply(commandEvent.getClient().getError() + " Error adding the guild to the db. Please contact the developer on the support server." + Bot.SUPPORT_INVITE_LINK);
                metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
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
        if(!guildDAO.updateMinNSFWRole(guild.getId(), mentionedRoles.get(0).getId())) {
            commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong! Please contact the devs on the support server. " +  Bot.SUPPORT_INVITE_LINK);
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
        }
        commandEvent.getMessage().addReaction(commandEvent.getClient().getSuccess()).queue();
    }
}
