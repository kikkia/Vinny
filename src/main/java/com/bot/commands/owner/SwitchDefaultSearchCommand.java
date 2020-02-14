package com.bot.commands.owner;

import com.bot.commands.OwnerCommand;
import com.bot.utils.Config;
import com.jagrosh.jdautilities.command.CommandEvent;

public class SwitchDefaultSearchCommand extends OwnerCommand {

    public SwitchDefaultSearchCommand() {
        this.name = "dsearch";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        String args = commandEvent.getArgs();

        if (args.equalsIgnoreCase("sc")) {
            Config config = Config.getInstance();
            config.setConfig(Config.DEFAULT_SEARCH_PROVIDER, "scsearch:");
            commandEvent.reactSuccess();
        } else if (args.equalsIgnoreCase("yt")){
            Config config = Config.getInstance();
            config.setConfig(Config.DEFAULT_SEARCH_PROVIDER, "ytsearch:");
            commandEvent.reactSuccess();
        } else {
            commandEvent.reactWarning();
        }
    }
}
