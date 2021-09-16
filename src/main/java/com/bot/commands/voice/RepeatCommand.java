package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import org.springframework.stereotype.Component;

@Component
public class RepeatCommand extends VoiceCommand {

	public RepeatCommand() {
		this.name = "repeat";
		this.arguments = "`all` for repeat all, no input for repeat one";
		this.help = "Toggles repeating the current track or all based on input";
	}

	@Override
	@Trace(operationName = "executeCommand", resourceName = "Repeat")
	protected void executeCommand(CommandEvent commandEvent) {
		VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
		if (handler == null) {
			commandEvent.reply(commandEvent.getClient().getWarning() + " I am not currently connected to voice.");
		}
		else {
			if (commandEvent.getArgs().equalsIgnoreCase("all")) {
				handler.setRepeatAll(!handler.isRepeatAll());
				String msg = handler.isRepeatAll() ? "on." : "off.";
				commandEvent.replySuccess("Repeat all is now " + msg);
			} else {
				handler.setRepeatOne(!handler.isRepeatOne());
				String msg = handler.isRepeatOne() ? "on." : "off.";
				commandEvent.replySuccess("Repeat one is now " + msg);
			}
		}
	}
}
