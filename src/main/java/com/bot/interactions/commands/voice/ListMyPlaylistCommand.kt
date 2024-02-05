package com.bot.interactions.commands.voice

import com.bot.interactions.commands.VoiceCommand
import com.bot.db.PlaylistDAO
import com.bot.interactions.InteractionEvent
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import net.dv8tion.jda.api.EmbedBuilder

class ListMyPlaylistCommand : VoiceCommand() {
    private val playlistDAO: PlaylistDAO = PlaylistDAO.getInstance()

    init {
        name = "myplaylists"
        help = "Returns all of the playlists you have"
    }

    @Trace(operationName = "executeCommand", resourceName = "myPlaylists")
    override fun executeCommand(commandEvent: CommandEvent) {
        val playlistList = playlistDAO.getPlaylistsForUser(commandEvent.author.id)
        if (playlistList.size == 0) {
            commandEvent.reply("You don't have any playlists. :cry:")
            return
        }
        val reply = StringBuilder()
        for (p in playlistList) {
            reply.append(p.id).append(": ")
            reply.append(p.name).append(" | ")
            reply.append("Total tracks: ").append(p.getTracks()!!.size)
            reply.append("\n")
        }
        val embedBuilder = EmbedBuilder()
        embedBuilder.addField("Playlists", reply.toString(), false)
        commandEvent.reply(embedBuilder.build())
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}