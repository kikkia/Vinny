package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;

public class StopCommand extends VoiceCommand {

	public StopCommand() {
		this.name = "stop";
		this.arguments = "";
		this.help = "Stops stream and clears the current playlist";
	}

	@Override
	//@trace(operationName = "executeCommand", resourceName = "Stop")
	protected void executeCommand(CommandEvent commandEvent) {
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
