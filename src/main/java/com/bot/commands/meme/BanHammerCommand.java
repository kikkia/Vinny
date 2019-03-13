package com.bot.commands.meme;

import com.bot.commands.MemeCommand;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.CommandEvent;

public class BanHammerCommand extends MemeCommand {

    private String hammer = "░░░░░░░░░░░░\n" +
            " ▄████▄░░░░░░░░░░░░░░░░░░░░\n" +
            "██████▄░░░░░░▄▄▄░░░░░░░░░░\n" +
            "░███▀▀▀▄▄▄▀▀▀░░░░░░░░░░░░░\n" +
            "░░░▄▀▀▀▄░░░█▀▀▄░▄▀▀▄░█▄░█░\n" +
            "░░░▄▄████░░█▀▀▄░█▄▄█░█▀▄█░\n" +
            "░░░░██████░█▄▄▀░█░░█░█░▀█░\n" +
            "░░░░░▀▀▀▀░░░░░░░░░░░░░░░░░";

    public BanHammerCommand() {
        this.name = "hammer";
        this.help = "bring out the hammer";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        commandEvent.reply(hammer);
    }
}
