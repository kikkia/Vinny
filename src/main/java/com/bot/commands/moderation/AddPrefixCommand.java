package com.bot.commands.moderation;

import com.bot.commands.ModerationCommand;
import com.bot.db.GuildDAO;
import com.bot.models.InternalGuild;
import com.bot.utils.GuildUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;

import java.util.ArrayList;
import java.util.Arrays;

public class AddPrefixCommand extends ModerationCommand {

    private GuildDAO guildDAO;

    public AddPrefixCommand() {
        this.name = "addprefix";
        this.help = "Adds one or more custom prefixes to the server.";
        this.arguments = "<One or more custom prefixes, separated by spaces>";

        guildDAO = GuildDAO.getInstance();
    }

    @Override
    //@trace(operationName = "executeCommand", resourceName = "AddPrefix")
    protected void executeCommand(CommandEvent commandEvent) {
        InternalGuild guild = guildDAO.getGuildById(commandEvent.getGuild().getId());

        if (commandEvent.getArgs().isEmpty()) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " You must specify at least one custom prefix to add.");
            return;
        }

        ArrayList<String> prefixes = guild.getPrefixList();
        prefixes.addAll(Arrays.asList(commandEvent.getArgs().split(" ")));
        String newPrefixes = GuildUtils.convertListToPrefixesString(prefixes);

        if (newPrefixes.length() > 254) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " Failed to add custom prefixes. The length of all" +
                    " prefixes on the server cannot be more than 250 characters. You can remove custom prefixes with the `removeprefix` command.");
            return;
        }

        if (guildDAO.updateGuildPrefixes(commandEvent.getGuild().getId(), prefixes)) {
            commandEvent.getMessage().addReaction(commandEvent.getClient().getSuccess()).queue();
        } else {
            commandEvent.reply(commandEvent.getClient().getError() + " Failed to update prefixes for server. Please contact a developer on the support server if this issue persists.");
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
        }
    }
}
