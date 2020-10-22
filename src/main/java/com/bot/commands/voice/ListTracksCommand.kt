package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.bot.voice.QueuedAudioTrack
import com.bot.voice.VoiceSendHandler
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.menu.Paginator
import java.util.*
import java.util.concurrent.TimeUnit

class ListTracksCommand(waiter: EventWaiter) : VoiceCommand() {
    private val builder: Paginator.Builder

    //@trace(operationName = "executeCommand", resourceName = "ListTracks")
    override fun executeCommand(commandEvent: CommandEvent) {
        val handler = commandEvent.guild.audioManager.sendingHandler as VoiceSendHandler?
        if (handler == null) {
            commandEvent.reply(commandEvent.client.warning + " I am not currently playing audio.")
            return
        }
        if (!handler.isPlaying) {
            commandEvent.reply(commandEvent.client.warning + " I am not currently playing audio.")
            return
        }
        val nowPlaying = handler.nowPlaying
        val tracks: List<QueuedAudioTrack> = ArrayList(handler.tracks)
        val trackNames = ArrayList<String>()

        trackNames.add("Now Playing: ${nowPlaying.track.info.title}")

        tracks.forEach {
            trackNames.add("${trackNames.size}: ${it.track.info.title}")
        }
        builder.setText("Tracks in the queue")
                .setColor(commandEvent.selfMember.color)
        builder.setItems(*trackNames.toTypedArray())
        builder.build().paginate(commandEvent.channel, 1)
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