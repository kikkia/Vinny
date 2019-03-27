package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.utils.CommandPermissions;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;

public class RepeatCommand extends VoiceCommand {

	public RepeatCommand() {
		this.name = "repeat";
		this.arguments = "";
		this.help = "Toggles repeating the current playlist";
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
		// Check the permissions to do the command
		if (!CommandPermissions.canExecuteCommand(this, commandEvent))
			return;

		VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
		if (handler == null) {
			commandEvent.reply(commandEvent.getClient().getWarning() + " I am not currently connected to voice.");
		}
		else if (handler.isRepeat()) {
			handler.setRepeat(false);
			commandEvent.reply(commandEvent.getClient().getSuccess() + " Repeat is now off.");
		}
		else {
			handler.setRepeat(true);
			commandEvent.reply(commandEvent.getClient().getSuccess() + " Repeat is now on.");
		}
	}
}
