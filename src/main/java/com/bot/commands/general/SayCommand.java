package com.bot.commands.general;

import com.bot.commands.GeneralCommand;
import com.bot.utils.FormattingUtils;
import com.jagrosh.jdautilities.command.CommandEvent;

public class SayCommand extends GeneralCommand {

    public SayCommand() {
        this.name = "say";
        this.help = "Repeat after you";
        this.arguments = "<Something to say>";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        commandEvent.reply(FormattingUtils.cleanSayCommand(commandEvent));
    }
}
