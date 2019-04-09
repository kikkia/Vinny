package com.bot.commands.meme;

import com.bot.commands.MemeCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class FeelsCommand extends MemeCommand {

    private String feels = "───────▄▀▀▀▀▀▀▀▀▀▀▄▄\n" +
            "────▄▀▀░░░░░░░░░░░░░▀▄\n" +
            "──▄▀░░░░░░░░░░░░░░░░░░▀▄\n" +
            "──█░░░░░░░░░░░░░░░░░░░░░▀▄\n" +
            "─▐▌░░░░░░░░▄▄▄▄▄▄▄░░░░░░░▐▌\n" +
            "─█░░░░░░░░░░░▄▄▄▄░░▀▀▀▀▀░░█\n" +
            "▐▌░░░░░░░▀▀▀▀░░░░░▀▀▀▀▀░░░▐▌\n" +
            "█░░░░░░░░░▄▄▀▀▀▀▀░░░░▀▀▀▀▄░█\n" +
            "█░░░░░░░░░░░░░░░░▀░░░▐░░░░░▐▌\n" +
            "▐▌░░░░░░░░░▐▀▀██▄░░░░░░▄▄▄░▐▌\n" +
            "─█░░░░░░░░░░░▀▀▀░░░░░░▀▀██░░█\n" +
            "─▐▌░░░░▄░░░░░░░░░░░░░▌░░░░░░█\n" +
            "──▐▌░░▐░░░░░░░░░░░░░░▀▄░░░░░█\n" +
            "───█░░░▌░░░░░░░░▐▀░░░░▄▀░░░▐▌\n" +
            "───▐▌░░▀▄░░░░░░░░▀░▀░▀▀░░░▄▀\n" +
            "───▐▌░░▐▀▄░░░░░░░░░░░░░░░░█\n" +
            "───▐▌░░░▌░▀▄░░░░▀▀▀▀▀▀░░░█\n" +
            "───█░░░▀░░░░▀▄░░░░░░░░░░▄▀\n" +
            "──▐▌░░░░░░░░░░▀▄░░░░░░▄▀\n" +
            "─▄▀░░░▄▀░░░░░░░░▀▀▀▀█▀\n" +
            "▀░░░▄▀░░░░░░░░░░▀░░░▀▀▀▀▄▄▄▄▄";

    public FeelsCommand() {
        this.name = "feels";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        commandEvent.reply(feels);
    }
}
