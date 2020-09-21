package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.commands.VoiceCommand;
import com.bot.voice.QueuedAudioTrack;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;

import java.util.concurrent.LinkedBlockingDeque;

public class RemoveTrackCommand extends VoiceCommand {
	private Bot bot;

	public RemoveTrackCommand(Bot bot) {
		this.name = "remove";
		this.help = "Removes a track from the current queue";
		this.arguments = "<(Position in queue) or (url of track) or (track name)>";
		this.bot = bot;
	}

	@Override
	//@trace(operationName = "executeCommand", resourceName = "RemoveTrack")
	protected void executeCommand(CommandEvent commandEvent) {
		if (commandEvent.getArgs().isEmpty()) {
			commandEvent.replyWarning("You need to tell me what track to remove");
			return;
		}

		VoiceSendHandler handler = bot.getHandler(commandEvent.getGuild());
		if (handler.getTracks().isEmpty()) {
			commandEvent.replyWarning("What? Boi the queue is empty! I can't remove anything.");
		}

		try {
			removeTrackAtIndex(commandEvent, handler, Integer.parseInt(commandEvent.getArgs()));
		} catch (NumberFormatException e) {
			removeByURLOrSearch(commandEvent, handler);
		}

	}

	// Removes a track in the queue at that index
	private void removeTrackAtIndex(CommandEvent event, VoiceSendHandler handler, int index) {
		if (index > handler.getTracks().size()) {
			event.replyWarning("Invalid input, there are " + handler.getTracks().size() + " tracks in the queue.");
			return;
		} else if (index <= 0) {
			event.replyWarning("You must give an index in the queue between 1 and " + handler.getTracks().size());
			return;
		}

		// In order to remove from the queue we need to build a new one
		int size = handler.getTracks().size();
		QueuedAudioTrack removedTrack = null;
		// Deep copy of queue to avoid race conditions
		LinkedBlockingDeque<QueuedAudioTrack> copyQ = new LinkedBlockingDeque<>(handler.getTracks());
		LinkedBlockingDeque<QueuedAudioTrack> rebuiltQ = new LinkedBlockingDeque<>();
		for (int i = 1; i <= size; i++) {
			QueuedAudioTrack track = copyQ.poll();
			if (i == index) {
				removedTrack = track;
				continue;
			}
			rebuiltQ.add(track);
		}
		handler.setTracks(rebuiltQ);

		if (removedTrack != null) {
			event.replySuccess("Removed `" + removedTrack.getTrack().getInfo().title + "` from the queue.");
		} else {
			event.replyWarning("Could not find any track at that index :thinking:");
		}
	}

	// Removes a track in the queue by url or search arg
	private void removeByURLOrSearch(CommandEvent event, VoiceSendHandler handler) {
		String args = event.getArgs();

		int size = handler.getTracks().size();
		QueuedAudioTrack removedTrack = null;
		// Deep copy of queue to avoid race conditions
		LinkedBlockingDeque<QueuedAudioTrack> copyQ = new LinkedBlockingDeque<>(handler.getTracks());
		LinkedBlockingDeque<QueuedAudioTrack> rebuiltQ = new LinkedBlockingDeque<>();
		for (int i = 0; i < size; i++) {
			QueuedAudioTrack track = copyQ.poll();
			// Remove all tracks with that url or name
			if (track.getTrack().getInfo().uri.equalsIgnoreCase(args)
					|| track.getTrack().getInfo().title.equalsIgnoreCase(args)){
				removedTrack = track;
				continue;
			}
			rebuiltQ.add(track);
		}
		handler.setTracks(rebuiltQ);

		if (removedTrack != null) {
			event.replySuccess("Removed all instances of `" + removedTrack.getTrack().getInfo().title + "` from the queue.");
		} else {
			event.replyWarning("Could not find any tracks with that url in the queue.");
		}
	}
}
