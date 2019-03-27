package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class SearchCommand extends VoiceCommand {

	public SearchCommand() {
		this.name = "search";
		this.arguments = "<Search terms>";
		this.help = "Searches youtube and reploies with a list of tracks to play.";
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		// TODO: Can bring search over from old but use Event waiter instead of timer

	}
}
