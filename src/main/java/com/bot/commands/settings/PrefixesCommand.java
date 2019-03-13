package com.bot.commands.settings;

import com.bot.commands.GeneralCommand;
import com.bot.db.GuildDAO;
import com.bot.models.InternalGuild;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.List;

public class PrefixesCommand extends GeneralCommand {

    private GuildDAO guildDAO;

    public PrefixesCommand() {
        this.name = "prefixes";
        this.arguments = "";
        this.help = "Lists all set prefixes for the server.";

        guildDAO = GuildDAO.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

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
