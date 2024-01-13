package com.bot.voice

import com.bot.db.models.ResumeAudioGuild
import com.bot.db.models.ResumeAudioTrack
import com.bot.exceptions.InvalidInputException
import com.bot.exceptions.NotInVoiceException
import com.bot.metrics.MetricsManager
import com.bot.models.enums.RepeatMode
import com.bot.utils.FormattingUtils
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.menu.OrderedMenu.Builder
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.LinkState
import dev.arbjerg.lavalink.client.TrackEndEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import org.apache.log4j.Logger
import java.text.Normalizer.Form

class GuildVoiceConnection(val guild: Guild) {
    val logger = Logger.getLogger(this::class.java.name)
    val lavalink: LavaLinkClient = LavaLinkClient.getInstance()
    val metricsManager = MetricsManager.instance!!
    private val trackProvider = TrackProvider()
    var currentVoiceChannel: VoiceChannel? = null
    var lastTextChannel: TextChannel? = null
    private var isPaused = false
    private var volume = 100
    var volumeLocked = false

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
        try {
            joinChannel(toJoin)
        } catch (e: Exception) {
            commandEvent.replyWarning("Failed to join voice channel: ${e.message}")
            cleanupPlayer()
            throw e
        }
    }

    private fun joinChannel(channel: VoiceChannel) {
        if (channel.guild.selfMember.voiceState == null) {
            throw NotInVoiceException("You are not in a voice channel! Please join one to use this command.")
        }
        if (channel == currentVoiceChannel && isConnected()) {
            return
        }

        if (!channel.guild.selfMember.hasPermission(channel, Permission.VIEW_CHANNEL)) {
            throw Exception("I do not have permissions to view that channel")
        }

        if (!channel.guild.selfMember.hasPermission(channel, Permission.VOICE_CONNECT)) {
            throw Exception("I do not have permissions to connect to that channel")
        }

        if (!channel.guild.selfMember.hasPermission(channel, Permission.VOICE_SPEAK)) {
            throw Exception("I do not have permissions to speak in that channel")
        }

        if (channel.userLimit > 0 && channel.userLimit <= channel.members.size) {
            throw Exception("Your voice channel is full")
        }

        val link = lavalink.getLink(channel.guild.idLong)

        if (link.state == LinkState.CONNECTED && currentVoiceChannel?.members?.contains(channel.guild.selfMember) == false) {
            link.destroyPlayer()
        }

        try {
            channel.jda.directAudioController.connect(channel)
            currentVoiceChannel = channel
        } catch (e: Exception) {
            logger.error("Failed to join voice channel $channel", e)
            throw e
        }
    }

    fun loadTrack(toLoad: String, commandEvent: CommandEvent) {
        metricsManager.markTrackLoaded()
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
            commandEvent.reply("Queued up `${track.track.info.title}`.")
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
        metricsManager.markTrackLoaded()
        link.loadItem(tracks[newIndex]).subscribe(
            PlaylistLLLoadHandler(this, commandEvent, loadingMessage, tracks, newIndex, failedCount))
    }


    fun queuePlaylist(tracks: List<String>, commandEvent: CommandEvent, loadingMessage: Message) {
        val link = getLink()
        if (link.state == LinkState.DISCONNECTED) {
            joinChannel(commandEvent)
        }
        metricsManager.markTrackLoaded()
        link.loadItem(tracks[0]).subscribe(PlaylistLLLoadHandler(this, commandEvent, loadingMessage, tracks, 0, 0))
        lastTextChannel = commandEvent.textChannel
    }

    fun queueResumeTrack(queuedTrack: QueuedAudioTrack?, loadingMessage: Message, resumeSetup: ResumeAudioGuild, index: Int, failedCount: Int) {
        val link = getLink()

        val newIndex = index+1
        updateLoadingMessage(loadingMessage, resumeSetup.tracks, newIndex, failedCount)
        if (queuedTrack != null) {
            if (trackProvider.getNowPlaying() == null) {
                playTrack(queuedTrack)
                seek(resumeSetup.tracks[index].position, queuedTrack)
            }
            trackProvider.addTrack(queuedTrack)
        }
        if (newIndex == resumeSetup.tracks.size) {
            return
        }
        metricsManager.markTrackLoaded()
        link.loadItem(resumeSetup.tracks[newIndex].trackUrl).subscribe(
            ResumeLLLoadHandler(this, loadingMessage, resumeSetup, newIndex, failedCount))
    }

    fun resumeAudioAfterReboot(resumeSetup: ResumeAudioGuild) {
        volume = resumeSetup.volume
        volumeLocked = resumeSetup.volumeLocked

        val link = getLink()
        if (link.state == LinkState.DISCONNECTED) {
            joinChannel(currentVoiceChannel!!)
        }
        metricsManager.markTrackLoaded()
        lastTextChannel!!.sendMessage("Resuming play after Vinny restart").queue()
        val loadingMessage = lastTextChannel!!.sendMessage("Loading previous queue...").complete()
        link.loadItem(resumeSetup.tracks[0].trackUrl).subscribe(ResumeLLLoadHandler(this, loadingMessage, resumeSetup, 0, 0))
    }

    fun searchForTrack(search: String, commandEvent: CommandEvent, message: Message, builder: Builder) {
        val link = getLink()
        if (link.state == LinkState.DISCONNECTED) {
            joinChannel(commandEvent)
        }
        metricsManager.markTrackLoaded()
        link.loadItem(search).subscribe(SearchLLLoadHandler(this, commandEvent, message, builder))
        lastTextChannel = commandEvent.textChannel
    }


    fun onTrackEnd(event: TrackEndEvent) {
        metricsManager.markTrackEnd()
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
        link.destroyPlayer().block()
        guild.jda.directAudioController.disconnect(guild)
        currentVoiceChannel = null
        trackProvider.clearAll()
        isPaused = false
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

    fun getRepeatMode() : RepeatMode {
        return trackProvider.getRepeateMode()
    }

    fun getVolume() : Int {
        return volume
    }

    fun setVolume(newVolume: Int) {
        if (newVolume > 200 || newVolume < 0) {
            throw NumberFormatException()
        }
        if (volumeLocked) {
            throw InvalidInputException("Volume is currently locked. It can be unlocked by a mod with the `~lockv` command.")
        }
        volume = newVolume
        getLink().createOrUpdatePlayer().setVolume(volume).block()
    }

    private fun seek(pos: Long, track: QueuedAudioTrack) {
        if (pos < 0 || pos > track.track.info.length) {
            throw NumberFormatException()
        }
        getLink().createOrUpdatePlayer().setPosition(pos).block()
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
        metricsManager.markTrackPlayed()
        getLink().createOrUpdatePlayer()
            .setVolume(volume)
            .setTrack(track.track).subscribe{
                if (lastTextChannel != null) {
                    sendNowPlayingUpdate()
                }
            }
    }

    private fun updateLoadingMessage(loadingMessage: Message, tracks: List<Any>, index: Int, failedCount: Int) {
        if (shouldUpdateLoadingMessage(tracks, index)) {
            var msg = "Loading playlist: ${index}/${tracks.size} completed. "
            if (failedCount > 0) {
                msg = msg.plus("Failed to load $failedCount tracks.")
            }
            loadingMessage.editMessage(msg).queue()
        }
    }

    // To not spam ratelimit, on larger playlists update less frequently
    private fun shouldUpdateLoadingMessage(tracks: List<Any>, index: Int): Boolean {
        val updateInterval = (tracks.size / 5).coerceAtLeast(1)
        return index % updateInterval == 0 || tracks.size == index
    }

    fun toggleVolumeLock() : Boolean {
        volumeLocked = !volumeLocked
        return volumeLocked
    }

    fun shuffleTracks() {
        trackProvider.shuffleQueue()
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
        try {
            lastTextChannel!!.sendMessage(msg).queue()
        } catch (e: Exception) {
            logger.error("Guild voice: could not send message to channel", e)
        }
    }

    private fun sendNowPlayingUpdate() {
        // If we sent the last message in the channel then just edit it
        lastTextChannel!!.history.retrievePast(1).queue { m ->
            val lastMessage: Message = m[0]
            val embed = FormattingUtils.getAudioTrackEmbed(trackProvider.getNowPlaying(), volume, trackProvider.getRepeateMode())
            if (lastMessage.author.id == lastTextChannel!!.jda.selfUser.id) {
                lastMessage.editMessageEmbeds(embed).queue()
            } else {
                lastTextChannel!!.sendMessageEmbeds(embed).queue()
            }
        }
    }
}