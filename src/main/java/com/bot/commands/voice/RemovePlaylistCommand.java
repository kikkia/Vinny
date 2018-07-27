package com.bot.commands.voice;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.logging.Logger;

public class RemovePlaylistCommand extends Command {
	private static final Logger LOGGER = Logger.getLogger(RemovePlaylistCommand.class.getName());

	public RemovePlaylistCommand() {
		// TODO: Init
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		// TODO: Remove from DB for user/guild?
	}
}
