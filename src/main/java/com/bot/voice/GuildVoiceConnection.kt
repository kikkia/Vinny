package com.bot.voice

import com.bot.exceptions.InvalidInputException
import com.bot.exceptions.NotInVoiceException
import com.bot.models.AudioTrack
import com.bot.models.enums.RepeatMode
import com.jagrosh.jdautilities.command.CommandEvent
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.LinkState
import dev.arbjerg.lavalink.client.TrackEndEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import org.apache.log4j.Logger

class GuildVoiceConnection(val guild: Guild) {
    val logger = Logger.getLogger(this::class.java.name)
    val lavalink: LavaLinkClient = LavaLinkClient.getInstance()
    private val trackProvider = TrackProvider()
    var currentVoiceChannel: VoiceChannel? = null
    var lastTextChannel: TextChannel? = null
    private var isPaused = false
    private var volume = 35
    private var volumeLocked = false

    fun setPaused(pause: Boolean) {
        lavalink.getLink(guild.idLong).getPlayer()
            .flatMap { it.setPaused(pause).asMono() }.subscribe{ this.isPaused = it.paused }
    }

    fun getPaused() : Boolean {
        return isPaused
    }

    private fun joinChannel(commandEvent: CommandEvent) {
        val toJoin = commandEvent.member.voiceState?.channel
            ?: throw NotInVoiceException(commandEvent.client.warning + " You are not in a voice channel! Please join one to use this command.")
        if (toJoin == currentVoiceChannel && isConnected()) {
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
        link.loadItem(toLoad).subscribe(LLLoadHandler(this, commandEvent))
        lastTextChannel = commandEvent.textChannel
    }

    fun queueTrack(track: QueuedAudioTrack, commandEvent: CommandEvent) {
        trackProvider.addTrack(track)
        if (trackProvider.getNowPlaying() == track) {
            playTrack(track)
        } else {
            // TODO
            commandEvent.reply("Queued up ${track.track.info.title}.")
        }
    }

    // Queue up track from playlist and then start load if there is a next track
    fun queuePlaylistTrack(queuedTrack: QueuedAudioTrack?, commandEvent: CommandEvent, loadingMessage: Message,
                           tracks: List<String>, index: Int, failedCount: Int) {
        val link = getLink()
        if (link.state == LinkState.DISCONNECTED) {
            joinChannel(commandEvent)
        }
        val newIndex = index+1
        updateLoadingMessage(loadingMessage, tracks, newIndex, failedCount)
        if (queuedTrack != null) {
            if (trackProvider.getNowPlaying() == null) {
                playTrack(queuedTrack)
            }
            trackProvider.addTrack(queuedTrack)
        }
        if (newIndex == tracks.size) {
            return
        }
        link.loadItem(tracks[newIndex]).subscribe(
            PlaylistLLLoadHandler(this, commandEvent, loadingMessage, tracks, newIndex, failedCount))
    }

    fun queuePlaylist(tracks: List<String>, commandEvent: CommandEvent, loadingMessage: Message) {
        val link = getLink()
        if (link.state == LinkState.DISCONNECTED) {
            joinChannel(commandEvent)
        }
        link.loadItem(tracks[0]).subscribe(PlaylistLLLoadHandler(this, commandEvent, loadingMessage, tracks, 0, 0))
        lastTextChannel = commandEvent.textChannel
    }

    fun onTrackEnd(event: TrackEndEvent) {
        nextTrack(false)
    }

    fun nextTrack(skipping: Boolean) {
        val next = trackProvider.nextTrack(skipping)
        if (next == null) {
            sendMessageToChannel("Finished playing all songs in queue.")
            cleanupPlayer()
            return
        }
        playTrack(next)
    }

    fun cleanupPlayer() {
        val link = getLink()
        link.destroyPlayer().subscribe()
        guild.jda.directAudioController.disconnect(guild)
        currentVoiceChannel = null
        trackProvider.clearAll()
        setPaused(false)
    }

    fun isConnected(): Boolean {
        return getLink().state == LinkState.CONNECTED
    }

    fun clearQueue() {
        trackProvider.clearQueue()
    }

    fun setRepeatMode(mode: RepeatMode) {
        trackProvider.setRepeatMode(mode)
    }

    fun getVolume() : Int {
        return volume
    }

    fun setVolume(newVolume: Int) {
        if (newVolume > 200 || newVolume < 0) {
            throw NumberFormatException()
        }
        if (volumeLocked) {
            throw InvalidInputException("Volume is currently locked. It can be unlocked by a mod with the `~lockvol` command.")
        }
        volume = newVolume
        getLink().createOrUpdatePlayer().setVolume(volume).block()
    }

    fun nowPlaying() : QueuedAudioTrack? {
        return trackProvider.getNowPlaying()
    }

    fun getPosition() : Long? {
        return getLink().getPlayer().block()?.position
    }

    fun getQueuedTracks() : List<QueuedAudioTrack> {
        return trackProvider.getQueued()
    }

    private fun getLink() : Link {
        return lavalink.getLink(guild.idLong)
    }

    private fun playTrack(track: QueuedAudioTrack) {
        getLink().createOrUpdatePlayer()
            .setVolume(volume)
            .setTrack(track.track).subscribe{
                if (lastTextChannel != null) {
                    val trackTitle = it.track!!.info.title
                    // TODO
                    sendMessageToChannel("Now playing: $trackTitle")
                }
            }
    }

    private fun updateLoadingMessage(loadingMessage: Message, tracks: List<String>, index: Int, failedCount: Int) {
        val msg = "Loading playlist: ${index}/${tracks.size} completed. "
        if (failedCount > 0) {
            msg.plus("Failed to load $failedCount tracks.")
        }
        loadingMessage.editMessage(msg).queue()
    }

    fun toggleVolumeLock() : Boolean {
        volumeLocked = !volumeLocked
        return volumeLocked
    }

    fun removeTrackAtIndex(index: Int): QueuedAudioTrack? {
        val tracks = trackProvider.getQueued()
        if (index > tracks.size) {
            throw InvalidInputException("Provided track index: $index is more than the total tracks queued: ${tracks.size}")
        } else if (index < 1) (
            throw InvalidInputException("Provided track index: $index must be >= 1.")
        )
        val newTracks = tracks.toMutableList()
        val toRemove = newTracks.removeAt(index-1)
        trackProvider.setTracks(newTracks)
        return toRemove
    }

    fun removeTrackByURLOrSearch(url: String): QueuedAudioTrack? {
        val tracks = trackProvider.getQueued()
        val toRemove = tracks.toMutableList().firstOrNull { it.track.info.title == url || it.track.info.uri == url }
        if (toRemove != null) {
            tracks.toMutableList().removeIf { it.track.info.title == url || it.track.info.uri == url }
            trackProvider.setTracks(tracks)
        }
        return toRemove
    }

    fun sendMessageToChannel(msg: String) {
        lastTextChannel!!.sendMessage(msg).queue()
    }
}