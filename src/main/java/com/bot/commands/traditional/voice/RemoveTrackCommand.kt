package com.bot.commands.traditional.voice

import com.bot.commands.traditional.VoiceCommand

import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class RemoveTrackCommand : VoiceCommand() {

    init {
        name = "remove"
        help = "Removes a track from the current queue"
        arguments = "<(Position in queue) or (url of track) or (track name)>"
    }

    @Trace(operationName = "executeCommand", resourceName = "RemoveTrack")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        if (commandEvent.args.isEmpty()) {
            commandEvent.replyWarning("You need to tell me what track to remove")
            return
        }
        if (voiceConnection.getQueuedTracks().isEmpty()) {
            commandEvent.replyWarning("What? Boi the queue is empty! I can't remove anything.")
        }
        val removed = if (commandEvent.args.toIntOrNull() != null) {
            voiceConnection.removeTrackAtIndex(commandEvent.args.toInt())
        } else {
            voiceConnection.removeTrackByURLOrSearch(commandEvent.args)
        }
        if (removed == null) {
            commandEvent.replyWarning("I was not able to find that track")
        } else {
            commandEvent.replySuccess("Removed: ${removed.track.info.title}")
        }
    }
}