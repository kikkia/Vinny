package com.bot.commands.traditional.moderation;

import com.bot.Bot;
import com.bot.commands.traditional.ModerationCommand;
import com.bot.db.GuildDAO;
import com.bot.models.InternalGuild;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.logging.Level;

public class SetModRoleCommand extends ModerationCommand {
    private final GuildDAO guildDAO;

    public SetModRoleCommand() {
        this.name = "modrole";
        this.help = "Sets the minimum role for moderation command.";
        this.arguments = "<Role mention or nothing for everyone>";
        this.guildDAO = GuildDAO.getInstance();
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "SetModRole")
    protected void executeCommand(CommandEvent commandEvent) {
        InternalGuild guild = null;
        Guild commandGuild = commandEvent.getGuild();

        try {
            guild = guildDAO.getGuildById(commandGuild.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Problem getting guild settings " + e.getMessage());
            commandEvent.reply(commandEvent.getClient().getError() + " There was a problem getting the settings for your guild. Please contact the developer on the support server. " + Bot.SUPPORT_INVITE_LINK);
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
            return;
        }

        if (guild == null) {
            logger.log(Level.WARNING, "Guild not found in db, attempting to add: " + commandGuild.getId());
            commandEvent.reply(commandEvent.getClient().getWarning() + " This guild was not found in my database. I am going to try to add it. Please standby.");

            if(!guildDAO.addGuild(commandGuild)) {
                logger.log(Level.SEVERE, "Failed to add the guild to the db");
                commandEvent.reply(commandEvent.getClient().getError() + " Error adding the guild to the db. Please contact the developer on the support server." + Bot.SUPPORT_INVITE_LINK);
                metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
                return;
            }
            commandEvent.reply(commandEvent.getClient().getSuccess() + " Added the guild to the database. Retrying");
            executeCommand(commandEvent);
            return;
        }

        // If nothing then set to all
        if (commandEvent.getArgs().isEmpty()) {
            if (!guildDAO.updateMinModRole(guild.getId(), commandEvent.getGuild().getPublicRole().getId())) {
                logger.log(Level.SEVERE, "Failed to update mod role for guild " + guild.getId());
                commandEvent.reply("Something went wrong. Please contact the developers on the support server. " + Bot.SUPPORT_INVITE_LINK);
                metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
            }
            commandEvent.reactSuccess();
            return;
        }


        List<Role> mentionedRoles = commandEvent.getMessage().getMentions().getRoles();
        if (mentionedRoles == null || mentionedRoles.isEmpty()) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " You must specify a role.");
            return;
        }

        // Just use the first mentioned roles
        if(!guildDAO.updateMinModRole(guild.getId(), mentionedRoles.get(0).getId())) {
            logger.log(Level.SEVERE, "Failed to update mod role for guild " + guild.getId());
            commandEvent.reply("Something went wrong. Please contact the developers on the support server. " + Bot.SUPPORT_INVITE_LINK);
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
        }
        commandEvent.reactSuccess();

    }
}
