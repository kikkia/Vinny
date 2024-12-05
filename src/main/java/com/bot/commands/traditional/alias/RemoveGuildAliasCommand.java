package com.bot.commands.traditional.alias;

import com.bot.commands.traditional.ModerationCommand;
import com.bot.db.AliasDAO;
import com.bot.db.GuildDAO;
import com.bot.models.Alias;
import com.bot.models.InternalGuild;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;

import java.sql.SQLException;

public class RemoveGuildAliasCommand extends ModerationCommand {

    private final GuildDAO guildDAO;
    private final AliasDAO aliasDAO;

    public RemoveGuildAliasCommand() {
        this.name = "removegalias";
        this.aliases = new String[]{"removeguildalias"};
        this.help = "removes an alias from the guild";
        this.arguments = "<alias>";
        this.aliasDAO = AliasDAO.getInstance();
        this.guildDAO = GuildDAO.getInstance();
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "RemoveGuildAlias")
    protected void executeCommand(CommandEvent commandEvent) {
        InternalGuild guild = guildDAO.getGuildById(commandEvent.getGuild().getId());

        Alias alias = guild.getAliasList().get(commandEvent.getArgs());

        if (alias == null) {
            commandEvent.replyWarning("Could not find that alias, make sure its typed correctly.");
            return;
        }

        try {
            aliasDAO.removeGuildAlias(alias, commandEvent.getGuild().getId());
            guild.getAliasList().remove(commandEvent.getArgs());
            guildDAO.updateGuildInCache(guild);
            commandEvent.reactSuccess();
        } catch (SQLException e) {
            logger.severe("Failed to remove alias", e);
            commandEvent.replyError("Failed to remove alias from db");
        }
    }
}
