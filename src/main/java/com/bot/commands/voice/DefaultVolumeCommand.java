package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.db.GuildDAO;
import com.jagrosh.jdautilities.command.CommandEvent;

public class DefaultVolumeCommand extends VoiceCommand {

    private GuildDAO guildDAO;

    public DefaultVolumeCommand() {
        this.name = "dvolume";
        this.arguments = "<Volume 1-200>";
        this.help = "Sets the default volume for the server";
        this.guildDAO = GuildDAO.getInstance();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        int newVolume;
        try{
            newVolume = Integer.parseInt(commandEvent.getArgs().split(" ")[0]);
            if (newVolume > 200 || newVolume < 0) {
                throw new NumberFormatException();
            }

            if (!guildDAO.updateGuildVolume(commandEvent.getGuild().getId(), newVolume)) {
                commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong updating the default volume.");
                metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
                return;
            }

            commandEvent.reactSuccess();
        }
        catch (NumberFormatException e) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " You must enter a volume between 0 and 200");
        }
    }

}
