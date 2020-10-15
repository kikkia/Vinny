package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.voice.QueuedAudioTrack;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;

import java.util.ArrayList;
import java.util.List;

public class ListTracksCommand extends VoiceCommand {

	public ListTracksCommand() {
		this.name = "list";
		this.arguments = "";
		this.help = "Lists the tracks currently in the queue";
		this.aliases = new String[]{"playlist", "lists", "queue"} ;
	}

	@Override
	//@trace(operationName = "executeCommand", resourceName = "ListTracks")
	protected void executeCommand(CommandEvent commandEvent) {
		VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();

		if (handler == null) {
			commandEvent.reply(commandEvent.getClient().getWarning() + " I am not currently playing audio.");
			return;
		}

		if (!handler.isPlaying()) {
			commandEvent.reply(commandEvent.getClient().getWarning() + " I am not currently playing audio.");
			return;
		}

		QueuedAudioTrack nowPlaying = handler.getNowPlaying();
		List<QueuedAudioTrack> tracks = new ArrayList<>(handler.getTracks());
		StringBuilder sb = new StringBuilder();
		sb.append("```\nNow Playing: " + nowPlaying.getTrack().getInfo().title + "\n");

		for (int i = 0; i < tracks.size(); i++) {
			if (i == 0)
				sb.append("Next: " + tracks.get(i).getTrack().getInfo().title + "\n");
			else
				sb.append((i + 1) + ": " + tracks.get(i).getTrack().getInfo().title + "\n");
		}
		sb.append("```");
		commandEvent.reply(sb.toString());
	}
}
