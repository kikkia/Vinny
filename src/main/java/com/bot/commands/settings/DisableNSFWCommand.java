package com.bot.commands.settings;

import com.bot.commands.ModerationCommand;
import com.bot.db.ChannelDAO;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.CommandEvent;

public class DisableNSFWCommand extends ModerationCommand {

    private ChannelDAO channelDAO;

    public DisableNSFWCommand() {
        this.name = "disablensfw";
        this.arguments = "";
        this.help = "Disables NSFW commands in the text channel it is posted in.";

        this.channelDAO = ChannelDAO.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        if (channelDAO.setTextChannelNSFW(commandEvent.getTextChannel(), false)) {
            commandEvent.getMessage().addReaction(commandEvent.getClient().getSuccess()).queue();
        } else {
            commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong, please try again later or contact an admin on the support server.");
        }
    }
}
