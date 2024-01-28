package com.bot.commands.moderation;

import com.bot.commands.ModerationCommand;
import com.bot.db.ChannelDAO;
import com.jagrosh.jdautilities.command.CommandEvent;

public class ToggleNSFWCommand extends ModerationCommand {

    private final ChannelDAO channelDAO;
    private final boolean enabled;

    public ToggleNSFWCommand(boolean enabled) {
        String state = enabled ? "Enable" : "Disable";
        this.name = state.toLowerCase() + "nsfw";
        this.arguments = "";
        this.help = state + " NSFW commands in the text channel it is posted in.";
        this.enabled = enabled;

        this.channelDAO = ChannelDAO.getInstance();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        if (channelDAO.setTextChannelNSFW(commandEvent.getTextChannel(), this.enabled)) {   
            commandEvent.getMessage().addReaction(commandEvent.getClient().getSuccess()).queue();
        } else {
            commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong, please try again later or contact an admin on the support server.");
            metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
        }
    }
}
