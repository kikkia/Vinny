package com.bot.commands.slash.voice

import com.bot.commands.slash.ExtSlashCommandEvent
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.menu.ButtonEmbedPaginator

class ListTracksSlashCommand(waiter: EventWaiter): VoiceSlashCommand() {
    val builder: ButtonEmbedPaginator.Builder = ButtonEmbedPaginator.Builder()

    // TODO: Bug: This crummy paginatior auto closes when only one page, we can hotwire that or smth
    init {
        this.name = "list-tracks"
        this.help = "Lists all tracks in the queue"
        this.builder.setEventWaiter(waiter)
        this.builder.setFinalAction {}
        postInit()
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val conn = provider.getGuildVoiceConnection(command.guild!!.idLong)
        if (conn == null) {
            command.replyWarningTranslated("VOICE_NO_PLAYING_TRACK")
            return
        }
        val tracks = conn.getQueuedTracks()
        // Due to the button embed builder being crummy we cannot set items per page
        // So we can just do it ourselves
        val pages = ArrayList<String>()
        var num = 0
        var page = ""
        tracks.forEach { track ->
            if (num == 0) {
                page += "Now Playing: ${conn.nowPlaying()}"
            } else if (num % 10 == 0) {
                pages.add(page)
                page = "$num: $track"
            } else {
                page += "\n$num: $track"
            }
            num++
        }
        pages.add(page)
        this.builder.setItems(*pages.toTypedArray())
        this.builder.setText("Queued tracks")
        this.builder.build().paginate(command.hook, 1)
    }
}