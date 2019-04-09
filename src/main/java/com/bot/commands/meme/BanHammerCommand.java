package com.bot.commands.meme;

import com.bot.commands.MemeCommand;
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
    protected void executeCommand(CommandEvent commandEvent) {
        commandEvent.reply(hammer);
    }
}
