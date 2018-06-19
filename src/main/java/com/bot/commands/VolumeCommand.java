package com.bot.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.logging.Logger;

public class VolumeCommand extends Command{
	private static final Logger LOGGER = Logger.getLogger(VolumeCommand.class.getName());

	public VolumeCommand() {
		// TODO: init
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		// TODO: Find the Audio player from the map and then change to the given volume
	}
}
