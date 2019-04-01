package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;

public class ResumeCommand extends VoiceCommand {

	public ResumeCommand() {
		this.name = "resume";
		this.arguments = "";
		this.help = "Resumes a paused Stream";
	}

	@Override
	protected void executeCommand(CommandEvent commandEvent) {
		metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());

		VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
		if (handler == null) {
			commandEvent.reply(commandEvent.getClient().getWarning() + " I am not connected to a voice channel.");
		}
		else {
			if (handler.getPlayer().isPaused()) {
				handler.getPlayer().setPaused(false);
				commandEvent.reply(commandEvent.getClient().getSuccess() + " Resumed stream.");
			}
			else {
				commandEvent.reply(commandEvent.getClient().getWarning() + " The stream is not paused.");
			}
		}
	}
}
