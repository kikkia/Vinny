package com.bot.commands.meme;

import com.bot.commands.MemeCommand;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.CommandEvent;

public class LennyCommand extends MemeCommand {

    String lenny = "───█───▄▀█▀▀█▀▄▄───▐█──────▄▀█▀▀█▀▄▄\n" +
            "──█───▀─▐▌──▐▌─▀▀──▐█─────▀─▐▌──▐▌─█▀\n" +
            "─▐▌──────▀▄▄▀──────▐█▄▄──────▀▄▄▀──▐▌\n" +
            "─█────────────────────▀█────────────█\n" +
            "▐█─────────────────────█▌───────────█\n" +
            "▐█─────────────────────█▌───────────█\n" +
            "─█───────────────█▄───▄█────────────█\n" +
            "─▐▌───────────────▀███▀────────────▐▌\n" +
            "──█──────────▀▄───────────▄▀───────█\n" +
            "───█───────────▀▄▄▄▄▄▄▄▄▄▀────────█";

    public LennyCommand() {
        this.name = "lenny";
        this.help = "prints a lenny face";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        commandEvent.reply(lenny);
    }
}
