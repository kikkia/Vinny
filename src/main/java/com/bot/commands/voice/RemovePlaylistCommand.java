package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.logging.Logger;

public class RemovePlaylistCommand extends VoiceCommand {
	private static final Logger LOGGER = Logger.getLogger(RemovePlaylistCommand.class.getName());

	public RemovePlaylistCommand() {
		// TODO: Init
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		// TODO: Remove from DB for user/guild?
	}
}
