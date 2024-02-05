package com.bot.interactions.commands.voice

import com.bot.interactions.commands.VoiceCommand
import com.bot.db.PlaylistDAO
import com.bot.interactions.InteractionEvent
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import net.dv8tion.jda.api.EmbedBuilder

class ListGuildPlaylistCommand : VoiceCommand() {
    private val playlistDAO: PlaylistDAO = PlaylistDAO.getInstance()

    init {
        name = "gplaylists"
        help = "Returns all of the playlists the guild has."
    }

    @Trace(operationName = "executeCommand", resourceName = "guildPlaylists")
    override fun executeCommand(commandEvent: CommandEvent) {
        val playlistList = playlistDAO.getPlaylistsForGuild(commandEvent.guild.id)
        if (playlistList.size == 0) {
            commandEvent.reply("Your guild doesn't have any playlists. :cry:")
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