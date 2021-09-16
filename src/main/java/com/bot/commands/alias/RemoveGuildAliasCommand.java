package com.bot.commands.alias;

import com.bot.commands.ModerationCommand;
import com.bot.db.AliasDAO;
import com.bot.db.GuildDAO;
import com.bot.models.Alias;
import com.bot.models.InternalGuild;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class RemoveGuildAliasCommand extends ModerationCommand {

    private GuildDAO guildDAO;
    private AliasDAO aliasDAO;

    public RemoveGuildAliasCommand(AliasDAO aliasDAO, GuildDAO guildDAO) {
        this.name = "removegalias";
        this.aliases = new String[]{"removeguildalias"};
        this.help = "removes an alias from the guild";
        this.arguments = "<alias>";
        this.aliasDAO = aliasDAO;
        this.guildDAO = guildDAO;
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
