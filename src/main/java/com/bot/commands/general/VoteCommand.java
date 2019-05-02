package com.bot.commands.general;

import com.bot.commands.GeneralCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class VoteCommand extends GeneralCommand {

    public VoteCommand() {
        this.name = "vote";
        this.help = "Support Vinny by upvoting on bot lists.";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        String message = "If you want to support Vinny, please go and vote for Vinny on these awesome sites: \n" +
                "https://discordbotlist.com/bots/276855867796881408\n" +
                "https://discord.boats/bot/vinny\n" +
                "https://bots.ondiscord.xyz/bots/276855867796881408\n" +
                "https://botsfordiscord.com/bot/276855867796881408\n" +
                "https://discordbots.org/bot/276855867796881408";

        commandEvent.reply(message);
    }
}
