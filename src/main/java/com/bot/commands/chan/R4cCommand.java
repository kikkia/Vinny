package com.bot.commands.chan;

import com.bot.commands.NSFWCommand;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.CommandEvent;

public class R4cCommand extends NSFWCommand {

    public R4cCommand() {
        this.name = "r4c";
        this.arguments = "<4chan board>";
        this.help = "Gets a random thread from a given 4chan board";
        this.aliases = new String[]{"random4chan", "r4chan", "random4c"};
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;


    }
}
