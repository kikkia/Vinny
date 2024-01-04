package com.bot.voice

import com.bot.exceptions.NotInVoiceException
import com.jagrosh.jdautilities.command.CommandEvent
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.LinkState
import dev.arbjerg.lavalink.client.TrackEndEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import org.apache.log4j.Logger

class GuildVoiceConnection(val guild: Guild) {
    val logger = Logger.getLogger(this::class.java.name)
    val lavalink: LavaLinkClient = LavaLinkClient.getInstance()
    val trackProvider = TrackProvider()
    var currentVoiceChannel: VoiceChannel? = null
    var lastTextChannel: TextChannel? = null
    private var isPaused = false

    fun setPaused(pause: Boolean) {
        lavalink.getLink(guild.idLong).getPlayer()
            .flatMap { it.setPaused(pause).asMono() }.subscribe{ this.isPaused = it.paused }
    }

    fun getPaused() : Boolean {
        return isPaused
    }

    fun joinChannel(commandEvent: CommandEvent) {
        val toJoin = commandEvent.member.voiceState?.channel
            ?: throw NotInVoiceException(commandEvent.client.warning + " You are not in a voice channel! Please join one to use this command.")
        if (toJoin == currentVoiceChannel) {
            return
        }

        if (!commandEvent.selfMember.hasPermission(toJoin, Permission.VIEW_CHANNEL)) {
            throw Exception("${commandEvent.client.warning} I do not have permissions to view that channel")
        }

        if (!commandEvent.selfMember.hasPermission(toJoin, Permission.VOICE_CONNECT)) {
            throw Exception("${commandEvent.client.warning} I do not have permissions to connect to that channel")
        }

        if (!commandEvent.selfMember.hasPermission(toJoin, Permission.VOICE_SPEAK)) {
            throw Exception("${commandEvent.client.warning} I do not have permissions to speak in that channel")
        }

        if (toJoin.userLimit > 0 && toJoin.userLimit <= toJoin.members.size) {
            throw Exception("${commandEvent.client.warning} Your voice channel is full")
        }

        val link = lavalink.getLink(commandEvent.guild.idLong)

        if (link.state == LinkState.CONNECTED && currentVoiceChannel?.members?.contains(commandEvent.guild.selfMember) == false) {
            link.destroyPlayer()
        }

        try {
            commandEvent.jda.directAudioController.connect(toJoin)
            currentVoiceChannel = toJoin
        } catch (e: Exception) {
            logger.error("Failed to join voice channel $toJoin", e)
            throw e
        }
    }

    fun loadTrack(toLoad: String, commandEvent: CommandEvent) {
        val link = getLink()
        if (link.state == LinkState.DISCONNECTED) {
            joinChannel(commandEvent)
        }
        link.loadItem(toLoad).subscribe(LLLoadHandler(this, link, commandEvent))
        lastTextChannel = commandEvent.textChannel
    }

    fun queueTrack(track: QueuedAudioTrack, commandEvent: CommandEvent) {
        trackProvider.addTrack(track)
        if (trackProvider.getNowPlaying() == track) {
            getLink().createOrUpdatePlayer()
                .setTrack(track.track)
                // TODO - default volume
                .setVolume(35)
                .subscribe { player ->
                    val playingTrack = player.track
                    val trackTitle = playingTrack!!.info.title
                    // TODO
                    commandEvent.reply(("Now playing: $trackTitle"))
                }
        } else {
            // TODO
            commandEvent.reply("Queued up ${track.track.info.title}.")
        }
    }

    fun onTrackEnd(event: TrackEndEvent) {
        val next = trackProvider.nextTrack()
        if (next == null) {
            cleanupPlayer()
            return
        }
        getLink().createOrUpdatePlayer()
            .setTrack(next.track).subscribe{
                if (lastTextChannel != null) {
                    val trackTitle = it.track!!.info.title
                    // TODO
                    lastTextChannel!!.sendMessage(("Now playing: $trackTitle")).queue()
                }
            }
    }

    fun cleanupPlayer() {
        val link = getLink()
        link.destroyPlayer().subscribe()
        guild.jda.directAudioController.disconnect(guild)
        currentVoiceChannel = null
        trackProvider.clearAll()
    }

    private fun getLink() : Link {
        return lavalink.getLink(guild.idLong)
    }
}