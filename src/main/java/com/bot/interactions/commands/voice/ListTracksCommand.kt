package com.bot.interactions.commands.voice

import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.VoiceCommand
import com.bot.voice.QueuedAudioTrack
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.menu.Paginator
import datadog.trace.api.Trace
import java.util.concurrent.TimeUnit

class ListTracksCommand(waiter: EventWaiter) : VoiceCommand() {
    private val builder: Paginator.Builder

    @Trace(operationName = "executeCommand", resourceName = "ListTracks")
    override fun executeCommand(commandEvent: CommandEvent) {
        val voiceConnection = guildVoiceProvider.getGuildVoiceConnection(commandEvent.guild)
        if (!voiceConnection.isConnected()) {
            commandEvent.reply(commandEvent.client.warning + " I am not currently playing audio.")
            return
        }
        val nowPlaying = voiceConnection.nowPlaying()
        val tracks: List<QueuedAudioTrack> = voiceConnection.getQueuedTracks()
        val trackNames = ArrayList<String>()

        if (nowPlaying == null) {
            commandEvent.replyWarning("No tracks are being played")
            return
        }

        trackNames.add("Now Playing: ${nowPlaying.track.info.title}")

        tracks.forEach {
            trackNames.add("${trackNames.size}: ${it.track.info.title}")
        }
        builder.setText("Tracks in the queue")
                .setColor(commandEvent.selfMember.color)
        builder.setItems(*trackNames.toTypedArray())
        builder.build().paginate(commandEvent.channel, 1)
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }

    init {
        name = "list"
        aliases = arrayOf("playlist", "lists", "queue")
        builder = Paginator.Builder()
                .setColumns(1)
                .setItemsPerPage(15)
                .useNumberedItems(false)
                .showPageNumbers(true)
                .setEventWaiter(waiter)
                .setTimeout(30, TimeUnit.SECONDS)
                .waitOnSinglePage(false)
                .setFinalAction { message -> message.clearReactions().queue() }
    }
}