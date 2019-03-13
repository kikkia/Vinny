package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.logging.Logger;

public class RemoveTrackCommand extends VoiceCommand {
	private static final Logger LOGGER = Logger.getLogger(RemoveTrackCommand.class.getName());

	public RemoveTrackCommand() {
		// TODO: init
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		// TODO: take track # and remove from current list
	}
}
