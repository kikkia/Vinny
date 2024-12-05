package com.bot.commands.traditional.moderation;

import com.bot.commands.traditional.ModerationCommand;
import com.bot.db.GuildDAO;
import com.bot.models.InternalGuild;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;

import java.util.ArrayList;
import java.util.Arrays;

public class RemovePrefixCommand extends ModerationCommand {

    private final GuildDAO guildDAO;

    public RemovePrefixCommand() {
        this.name = "removeprefix";
        this.help = "Removes one or more custom prefixes from the current custom prefixes.";
        this.arguments = "<One or more custom prefixes, separated by spaces>";

        guildDAO = GuildDAO.getInstance();
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "RemovePrefix")
    protected void executeCommand(CommandEvent commandEvent) {
        InternalGuild guild = guildDAO.getGuildById(commandEvent.getGuild().getId());

        if (commandEvent.getArgs().isEmpty()) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " You must specify at least one custom prefix to remove.");
            return;
        }

        ArrayList<String> prefixes = guild.getPrefixList();
        prefixes.removeAll(Arrays.asList(commandEvent.getArgs().split(" ")));

        if (guildDAO.updateGuildPrefixes(commandEvent.getGuild().getId(), prefixes)) {
            commandEvent.reactSuccess();
        } else {
            commandEvent.reply(commandEvent.getClient().getError() + " Failed to update prefixes for server. Please contact a developer on the support server if this issue persists.");
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
        }
    }
}
