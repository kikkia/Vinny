package com.bot.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

public class TestCommand extends Command{

    public TestCommand() {
        this.name = "choose";
        this.help = "make a decision";
        this.arguments = "<item> <item> ...";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        if (commandEvent.getArgs().isEmpty()) {
            commandEvent.replyWarning("You must enter some things for me to choose from...");
        }
        else {
            String[] items = commandEvent.getArgs().split("\\s+");

            if (items.length == 1) {
                commandEvent.replyWarning("Well I guess I must choose " + items[0]);
            }
            else {
                commandEvent.replySuccess("I choose `" + items[(int)(Math.random()*items.length)] + "`");
            }
        }

    }
}
