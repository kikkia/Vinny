package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;

public class SkipCommand extends VoiceCommand {

	public SkipCommand() {
		this.name = "skip";
		this.arguments = "";
		this.help = "skips to the next track";
	}

	@Override
	//@trace(operationName = "executeCommand", resourceName = "SkipTrack")
	protected void executeCommand(CommandEvent commandEvent) {
		VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
		if (handler == null) {
			commandEvent.reply(commandEvent.getClient().getWarning() + " I am not connected to a voice channel.");
		}
		else {
			if (handler.skipTrack()) {
				commandEvent.getMessage().addReaction(commandEvent.getClient().getSuccess()).queue();
			}
			else {
				commandEvent.reply(commandEvent.getClient().getWarning() + " Unable to skip to the next track");
			}
		}
	}
}
