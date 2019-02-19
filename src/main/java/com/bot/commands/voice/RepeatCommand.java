package com.bot.commands.voice;

import com.bot.utils.CommandCategories;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.logging.Logger;

public class RepeatCommand extends Command {
	private static final Logger LOGGER = Logger.getLogger(RepeatCommand.class.getName());

	public RepeatCommand() {
		this.name = "repeat";
		this.arguments = "";
		this.help = "Toggles repeating the current playlist";
		this.category = CommandCategories.VOICE;
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
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
