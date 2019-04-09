package com.bot.commands.meme;

import com.bot.commands.MemeCommand;
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
    protected void executeCommand(CommandEvent commandEvent) {
        commandEvent.reply(salt);
    }
}
