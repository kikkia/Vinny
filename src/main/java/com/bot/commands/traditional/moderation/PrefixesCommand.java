package com.bot.commands.traditional.moderation;

import com.bot.commands.traditional.GeneralCommand;
import com.bot.db.GuildDAO;
import com.bot.models.InternalGuild;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;

import java.util.List;

public class PrefixesCommand extends GeneralCommand {

    private final GuildDAO guildDAO;

    public PrefixesCommand() {
        this.name = "prefixes";
        this.arguments = "";
        this.help = "Lists all set prefixes for the server.";

        guildDAO = GuildDAO.getInstance();
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "Prefixes")
    protected void executeCommand(CommandEvent commandEvent) {
        InternalGuild guild = guildDAO.getGuildById(commandEvent.getGuild().getId());
        List<String> prefixes = guild.getPrefixList();

        if (prefixes.isEmpty()) {
            commandEvent.reply("There are not custom prefixes configured for this server. To add custom prefixes use the" +
                    " `addprefix` command.\nDefault prefixes: `~` and <@" + commandEvent.getSelfUser().getId() + ">");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("The current custom prefixes are: \n");
        for (String prefix : prefixes) {
            sb.append("`").append(prefix).append("`").append("\n");
        }
        commandEvent.reply(sb.toString());
    }
}
