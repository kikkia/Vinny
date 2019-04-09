package com.bot.commands.general;

import com.bot.commands.GeneralCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class StatsCommand extends GeneralCommand {

    public static final String DASHBOARD_LINK = "https://p.datadoghq.com/sb/f38e74b49-cb74461cc4";

    public StatsCommand() {
        this.name = "stats";
        this.help = "Gives a link to detailed vinny statistics";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {

        commandEvent.reply("To find detailed stats, you can find the stats dashboard here: " + DASHBOARD_LINK);
    }
}
