package com.bot.commands.voice;

import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.logging.Logger;

public class StopCommand extends Command {
	private static final Logger LOGGER = Logger.getLogger(StopCommand.class.getName());

	public StopCommand() {
		this.name = "stop";
		this.arguments = "";
		this.help = "Stops stream and clears the current playlist";
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		// TODO: Add more permissions checks
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
