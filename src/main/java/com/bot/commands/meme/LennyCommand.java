package com.bot.commands.meme;

import com.bot.commands.MemeCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class LennyCommand extends MemeCommand {

    private String lenny = "───█───▄▀█▀▀█▀▄▄───▐█──────▄▀█▀▀█▀▄▄\n" +
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
    protected void executeCommand(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());

        commandEvent.reply(lenny);
    }
}
