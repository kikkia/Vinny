package com.bot.commands.voice;

import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.logging.Logger;

public class ResumeCommand extends Command {
	private static final Logger LOGGER = Logger.getLogger(ResumeCommand.class.getName());

	public ResumeCommand() {
		this.name = "resume";
		this.arguments = "";
		this.help = "Resumes a paused Stream";
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
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
