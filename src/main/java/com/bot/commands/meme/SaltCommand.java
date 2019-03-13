package com.bot.commands.meme;

import com.bot.commands.MemeCommand;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.CommandEvent;

public class SaltCommand extends MemeCommand {

    private String salt = "▒▒▒▒▒▒▄▄██████▄\n" +
            "▒▒▒▒▒▒▒▒▒▒▄▄████████████▄\n" +
            "▒▒▒▒▒▒▄▄██████████████████\n" +
            "▒▒▒▄████▀▀▀██▀██▌███▀▀▀████\n" +
            "▒▒▐▀████▌▀██▌▀▐█▌████▌█████▌\n" +
            "▒▒█▒▒▀██▀▀▐█▐█▌█▌▀▀██▌██████\n" +
            "▒▒█▒▒▒▒████████████████████▌\n" +
            "▒▒▒▌▒▒▒▒█████░░░░░░░██████▀\n" +
            "▒▒▒▀▄▓▓▓▒███░░░░░░█████▀▀\n" +
            "▒▒▒▒▀░▓▓▒▐█████████▀▀▒\n" +
            "▒▒▒▒▒░░▒▒▐█████▀▀▒▒▒▒▒▒\n" +
            "▒▒░░░░░▀▀▀▀▀▀▒▒▒▒▒▒▒▒▒\n" +
            "▒▒▒░░░░░░░░▒▒";

    public SaltCommand() {
        this.name = "salt";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        commandEvent.reply(salt);
    }
}
