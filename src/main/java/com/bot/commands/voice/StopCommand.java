package com.bot.commands.voice;

import com.bot.utils.CommandCategories;
import com.bot.utils.CommandPermissions;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.logging.Logger;

public class StopCommand extends Command {
	private static final Logger LOGGER = Logger.getLogger(StopCommand.class.getName());

	public StopCommand() {
		this.name = "stop";
		this.arguments = "";
		this.help = "Stops stream and clears the current playlist";
		this.category = CommandCategories.VOICE;
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
	    // Command permissions check
		if (!CommandPermissions.canExecuteCommand(this, commandEvent))
		    return;

		VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
		if (handler == null) {
			commandEvent.reply(commandEvent.getClient().getWarning() + " I am not connected to a voice channel.");
		}
		else {
			handler.stop();
			commandEvent.reply(commandEvent.getClient().getSuccess() + " Stopped audio stream");
			commandEvent.getGuild().getAudioManager().closeAudioConnection();
		}
	}
}
