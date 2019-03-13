package com.bot.commands.settings;

import com.bot.commands.ModerationCommand;
import com.bot.db.GuildDAO;
import com.bot.utils.CommandPermissions;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.logging.Logger;

public class DefaultVolumeCommand extends ModerationCommand {

    private static final Logger LOGGER = Logger.getLogger(com.bot.commands.voice.VolumeCommand.class.getName());
    private GuildDAO guildDAO;

    public DefaultVolumeCommand() {
        this.name = "dvolume";
        this.arguments = "<Volume 1-200>";
        this.help = "Sets the default volume for the server";
        this.guildDAO = GuildDAO.getInstance();
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        int newVolume;
        try{
            newVolume = Integer.parseInt(commandEvent.getArgs().split(" ")[0]);
            if (newVolume > 200 || newVolume < 0) {
                throw new NumberFormatException();
            }

            if (!guildDAO.updateGuildVolume(commandEvent.getGuild().getId(), newVolume)) {
                commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong updating the default volume.");
                return;
            }

            commandEvent.getMessage().addReaction(commandEvent.getClient().getSuccess()).queue();
        }
        catch (NumberFormatException e) {
            commandEvent.reply(commandEvent.getClient().getError() + " You must enter a volume between 0 and 200");
        }
    }

}
