package com.bot.commands.meme;

import com.bot.commands.MemeCommand;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.CommandEvent;

public class KappaCommand extends MemeCommand {

    private String kappa = "░░░░░░░░░░░░░░░░░░\n" +
            "░░░░▄▀▀▀▀▀█▀▄▄▄▄░░░░\n" +
            "░░▄▀▒▓▒▓▓▒▓▒▒▓▒▓▀▄░░\n" +
            "▄▀▒▒▓▒▓▒▒▓▒▓▒▓▓▒▒▓█░\n" +
            "█▓▒▓▒▓▒▓▓▓░░░░░░▓▓█░\n" +
            "█▓▓▓▓▓▒▓▒░░░░░░░░▓█░\n" +
            "▓▓▓▓▓▒░░░░░░░░░░░░█░\n" +
            "▓▓▓▓░░░░▄▄▄▄░░░▄█▄▀░\n" +
            "░▀▄▓░░▒▀▓▓▒▒░░█▓▒▒░░\n" +
            "▀▄░░░░░░░░░░░░▀▄▒▒█░\n" +
            "░▀░▀░░░░░▒▒▀▄▄▒▀▒▒█░\n" +
            "░░▀░░░░░░▒▄▄▒▄▄▄▒▒█░\n" +
            "░░░▀▄▄▒▒░░░░▀▀▒▒▄▀░░\n" +
            "░░░░░▀█▄▒▒░░░░▒▄▀░░░\n" +
            "░░░░░░░░▀▀█▄▄▄▄▀";

    public KappaCommand() {
        this.name = "kappa";
        this.help = "prints a kappa face";
    }


    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        commandEvent.reply(kappa);
    }
}
