package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.commands.VoiceCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class RemoveTrackCommand extends VoiceCommand {
	private Bot bot;

	public RemoveTrackCommand(Bot bot) {
		this.name = "remove";
		this.help = "Removes a track from the current queue";
		this.arguments = "<(Position in queue) or (url of track) or (search terms)>";
		this.bot = bot;
	}

	@Override
	protected void executeCommand(CommandEvent commandEvent) {

	}
}
