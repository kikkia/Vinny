package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.utils.CommandPermissions;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.logging.Logger;

public class VolumeCommand extends VoiceCommand {

	public VolumeCommand() {
		this.name = "volume";
		this.arguments = "<Volume 1-200>";
		this.help = "Sets the players volume";
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
		// Check the permissions to do the command
		if (!CommandPermissions.canExecuteCommand(this, commandEvent))
			return;

		VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
		int newVolume;
		try{
			newVolume = Integer.parseInt(commandEvent.getArgs().split(" ")[0]);
			if (newVolume > 200 || newVolume < 0) {
				throw new NumberFormatException();
			}
		}
		catch (NumberFormatException e) {
			commandEvent.reply(commandEvent.getClient().getError() + " You must enter a volume between 0 and 200");
			return;
		}

		if (handler == null) {
			commandEvent.reply(commandEvent.getClient().getWarning() + " I am not connected to a voice channel.");
		}
		else {
			handler.getPlayer().setVolume(newVolume);
		}
	}
}
