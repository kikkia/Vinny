package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.utils.CommandCategories;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.logging.Logger;

public class VoiceStatsCommand extends Command {
	private static final Logger LOGGER = Logger.getLogger(VoiceStatsCommand.class.getName());

	public VoiceStatsCommand(Bot bot) {
		// TODO: Figure out what we need to do this command and init
		this.category = CommandCategories.VOICE;
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		// TODO: Go through the shards and get some stats about the current playing streams
	}
}
