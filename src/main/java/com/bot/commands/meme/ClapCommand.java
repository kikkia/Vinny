package com.bot.commands.meme;

import com.bot.commands.MemeCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

import static com.bot.utils.FormattingUtils.clapify;
import static com.bot.utils.GuildUtils.getLastMessageFromChannel;

public class ClapCommand extends MemeCommand {

    public ClapCommand() {
        this.name = "clap";
        this.help = ":clap:";
        this.arguments = "<message or nothing>";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        if (commandEvent.getArgs().isEmpty()) {
            commandEvent.reply(clapify(getLastMessageFromChannel(commandEvent.getTextChannel(), true).getContentStripped()));
        }
        else
            commandEvent.reply(clapify(commandEvent.getMessage().getContentStripped()));
    }
}
