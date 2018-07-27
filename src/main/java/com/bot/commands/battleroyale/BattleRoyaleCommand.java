package com.bot.commands.battleroyale;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.logging.Logger;

public class BattleRoyaleCommand extends Command {
    private static final Logger LOGGER = Logger.getLogger(BattleRoyaleCommand.class.getName());

    public BattleRoyaleCommand() {
        this.name = "battleroyale";
        this.arguments = "<\"\"|@role|@user @user...>";
        this.help = "Throws specified users (no specified user does all in server) into a simulated battle royale, " +
                "because 2018 or something";
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        // TODO: Implement with weapons and whatever else.
    }
}
