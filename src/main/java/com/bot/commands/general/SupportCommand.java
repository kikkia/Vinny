package com.bot.commands.general;

import com.bot.commands.GeneralCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class SupportCommand extends GeneralCommand {

    public SupportCommand() {
        this.name = "support";
        this.help = "Gives a link to the support server";
        this.aliases = new String[]{"bug", "report", "helppls", "pls"};
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {

        commandEvent.reply("To report bugs, suggest features, or just hang out, join the Vinny support server: https://discord.gg/XMwyzxZ");
    }
}
