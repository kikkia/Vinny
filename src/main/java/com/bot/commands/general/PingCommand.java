package com.bot.commands.general;

import com.bot.utils.CommandCategories;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class PingCommand extends Command {

    public PingCommand() {
        this.name = "ping";
        this.category = CommandCategories.GENERAL;
        this.guildOnly = false;
        this.help = "Gets the ping from Vinny to discord.";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.reply(commandEvent.getJDA().getPing() + "ms");
    }
}
