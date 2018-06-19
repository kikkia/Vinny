package com.bot.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.logging.Logger;

public class SearchCommand extends Command {
	private static final Logger LOGGER = Logger.getLogger(SearchCommand.class.getName());

	public SearchCommand() {
		// TODO: Init
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		// TODO: Can bring search over from old but use Event waiter instead of timer
	}
}
