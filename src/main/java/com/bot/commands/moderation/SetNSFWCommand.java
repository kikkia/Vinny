package com.bot.commands.moderation;

import com.bot.Bot;
import com.bot.commands.ModerationCommand;
import com.bot.db.GuildDAO;
import com.bot.models.InternalGuild;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.logging.Level;

public class SetNSFWCommand extends ModerationCommand {
    private final GuildDAO guildDAO;

    public SetNSFWCommand() {
        this.name = "nsfwrole";
        this.help = "Sets the minimum required to use NSFW commands.";
        this.arguments = "<Role mention or empty for everyone>";
        this.guildDAO = GuildDAO.getInstance();
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "SetNSFWRole")
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

            if (!guildDAO.addGuild(commandGuild)) {
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
            if (!guildDAO.updateMinNSFWRole(guild.getId(), commandEvent.getGuild().getPublicRole().getId())) {
                logger.log(Level.SEVERE, "Failed to update nsfw role for guild " + guild.getId());
                commandEvent.reply("Something went wrong. Please contact the developers on the support server. " + Bot.SUPPORT_INVITE_LINK);
                metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
            }
            commandEvent.reactSuccess();
            return;
        }


        List<Role> mentionedRoles = commandEvent.getMessage().getMentions().getRoles();
        if (mentionedRoles.isEmpty()) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " You must specify a role.");
            return;
        }

        // Just use the first mentioned roles
        if(!guildDAO.updateMinNSFWRole(guild.getId(), mentionedRoles.get(0).getId())) {
            commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong! Please contact the devs on the support server. " +  Bot.SUPPORT_INVITE_LINK);
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
        }
        commandEvent.reactSuccess();
    }
}
