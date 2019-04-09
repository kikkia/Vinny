package com.bot.commands.settings;

import com.bot.Bot;
import com.bot.commands.GeneralCommand;
import com.bot.db.GuildDAO;
import com.bot.models.InternalGuild;
import com.bot.utils.CommandCategories;
import com.bot.utils.Logger;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;

import java.util.logging.Level;

public class GetSettingsCommand extends GeneralCommand {
    private static final Logger LOGGER = new Logger(GetSettingsCommand.class.getName());
    private GuildDAO guildDAO;

    public GetSettingsCommand() {
        this.name = "settings";
        this.help = "Gets the settings for the guild. (Requires mod command permissions)";
        this.arguments = "";

        // General since we are not setting any data
        this.guildDAO = GuildDAO.getInstance();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
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

        Role generalRole = commandGuild.getRoleById(guild.getRequiredPermission(CommandCategories.GENERAL));
        Role modRole = commandGuild.getRoleById(guild.getRequiredPermission(CommandCategories.MODERATION));
        Role voiceRole = commandGuild.getRoleById(guild.getRequiredPermission(CommandCategories.VOICE));
        Role nsfwRole = commandGuild.getRoleById(guild.getRequiredPermission(CommandCategories.NSFW));

        String generalName = generalRole != null ? generalRole.getName() : "Role not found";
        String modName = modRole != null ? modRole.getName() : "Role not found";
        String voiceName = voiceRole != null ? voiceRole.getName() : "Role not found";
        String nsfwName = nsfwRole != null ? nsfwRole.getName() : "Role not found";

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Settings for " + guild.getName());
        builder.addField("Default Volume", guild.getVolume() + "", true);
        builder.addField("Min Command Role", generalName, false);
        builder.addField("Min Mod Command Role", modName, false);
        builder.addField("Min Voice Command Role", voiceName, false);
        builder.addField("Min NSFW Command Role", nsfwName, false);

        commandEvent.reply(builder.build());
    }
}
