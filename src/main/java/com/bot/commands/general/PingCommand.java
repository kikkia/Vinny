package com.bot.commands.general;

import com.bot.commands.GeneralCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class PingCommand extends GeneralCommand {

    public PingCommand() {
        this.name = "ping";
        this.guildOnly = false;
        this.help = "Gets the ping from Vinny to discord.";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {

        commandEvent.reply(commandEvent.getJDA().getGatewayPing() + "ms");
    }
}
