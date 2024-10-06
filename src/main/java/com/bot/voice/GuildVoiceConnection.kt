package com.bot.voice

import com.bot.db.GuildDAO
import com.bot.db.OauthConfigDAO
import com.bot.db.models.OauthConfig
import com.bot.db.models.ResumeAudioGuild
import com.bot.exceptions.InvalidInputException
import com.bot.exceptions.NotInVoiceException
import com.bot.exceptions.OauthNotEnabledException
import com.bot.exceptions.UserExposableException
import com.bot.metrics.MetricsManager
import com.bot.models.enums.RepeatMode
import com.bot.utils.FormattingUtils
import com.bot.utils.LLUtils
import com.bot.utils.Oauth2Utils
import com.bot.utils.VinnyConfig
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.menu.OrderedMenu.Builder
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.LinkState
import dev.arbjerg.lavalink.client.event.TrackEndEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import org.apache.log4j.Logger
import reactor.kotlin.core.publisher.toMono
import java.time.Instant
import java.time.temporal.ChronoField
import java.util.*

class GuildVoiceConnection(val guild: Guild) {
    val logger = Logger.getLogger(this::class.java.name)
    val lavalink: LavaLinkClient = LavaLinkClient.getInstance()
    val metricsManager = MetricsManager.instance!!
    private val trackProvider = TrackProvider()
    private val autoplayQueue = LinkedList<String>()
    var currentVoiceChannel: VoiceChannel? = null
    var lastTextChannel: TextChannel? = null
    private var isPaused = false
    private var volume = GuildDAO.getInstance().getGuildById(guild.id).volume ?: 100
    var autoplay = GuildDAO.getInstance().isGuildPremium(guild.id)
    var volumeLocked = false
    private var created = Instant.now()
    var region = "N/A"
    var oauthConfig: OauthConfig? = null
    var oauthConfigDAO = OauthConfigDAO.getInstance()

    fun setPaused(pause: Boolean) {
        lavalink.getLink(guild.idLong).getPlayer()
            .flatMap { it.setPaused(pause).toMono() }.subscribe{ this.isPaused = it.paused }
    }

    fun getPaused() : Boolean {
        return isPaused
    }

    private fun joinChannel(commandEvent: CommandEvent) {
        val toJoin = commandEvent.member.voiceState?.channel
            ?: throw NotInVoiceException(commandEvent.client.warning + " You are not in a voice channel! Please join one to use this command.")
        try {
            joinChannel(toJoin)
        } catch (e: UserExposableException) {
            throw e
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
            link.destroy()
        }

        findOauth(channel)

        try {
            channel.jda.directAudioController.connect(channel)
            currentVoiceChannel = channel
        } catch (e: Exception) {
            logger.error("Failed to join voice channel $channel", e)
            throw e
        }
        GuildDAO.getInstance().updateLastVoiceConnectTime(guild.id)
    }

    fun loadTrack(toLoad: String, commandEvent: CommandEvent) {
        val link = getLink()
        if (link.state == LinkState.DISCONNECTED) {
            joinChannel(commandEvent)
        }
        injectOauth(toLoad)
        metricsManager.markTrackLoaded()
        link.loadItem(toLoad).subscribe(LLLoadHandler(this, commandEvent))
        lastTextChannel = commandEvent.textChannel
    }

    private fun loadAutoplayTrack(toLoad: String) {
        val link = getLink()
        injectOauth(toLoad)
        metricsManager.markTrackLoaded()
        link.loadItem(toLoad).subscribe(AutoplayLoadHandler(this))
    }

    fun queueTrack(track: QueuedAudioTrack) {
        trackProvider.addTrack(track)
        if (trackProvider.getNowPlaying() == track) {
            playTrack(track)
        } else {
            lastTextChannel!!.sendMessage("Queued up `${track.track.info.title}`.").queue()
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
        injectOauth(tracks[newIndex])
        metricsManager.markTrackLoaded()
        link.loadItem(tracks[newIndex]).subscribe(
            PlaylistLLLoadHandler(this, commandEvent, loadingMessage, tracks, newIndex, failedCount))
    }


    fun queuePlaylist(tracks: List<String>, commandEvent: CommandEvent, loadingMessage: Message) {
        val link = getLink()
        if (link.state == LinkState.DISCONNECTED) {
            joinChannel(commandEvent)
        }
        injectOauth(tracks[0])
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
        injectOauth(resumeSetup.tracks[newIndex].trackUrl)
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
        lastTextChannel!!.sendMessage("Resuming play after Vinny restart").queue()
        val loadingMessage = lastTextChannel!!.sendMessage("Loading previous queue...").complete()
        injectOauth(resumeSetup.tracks[0].trackUrl)
        metricsManager.markTrackLoaded()
        link.loadItem(resumeSetup.tracks[0].trackUrl).subscribe(ResumeLLLoadHandler(this, loadingMessage, resumeSetup, 0, 0))
    }

    fun searchForTrack(search: String, commandEvent: CommandEvent, message: Message, builder: Builder) {
        val link = getLink()
        if (link.state == LinkState.DISCONNECTED) {
            joinChannel(commandEvent)
        }
        injectOauth(search)
        metricsManager.markTrackLoaded()
        link.loadItem(search).subscribe(SearchLLLoadHandler(this, commandEvent, message, builder))
        lastTextChannel = commandEvent.textChannel
    }


    fun onTrackEnd(event: TrackEndEvent) {
        metricsManager.markTrackEnd(event.endReason.name, event.endReason.mayStartNext)
        if (event.endReason.mayStartNext) {
            nextTrack(false)
        }
    }

    fun nextTrack(skipping: Boolean) {
        // Make sure autoplay is populated before going to next track
        refreshAutoPlay()

        val next = trackProvider.nextTrack(skipping)
        if (next == null) {
            if (!autoplay) {
                sendMessageToChannel("Finished playing all songs in queue.")
                cleanupPlayer()
                return
            }
            // Autoplay failed to load tracks and we have no now playing. Fail softly.
            if (autoplayQueue.isEmpty()) {
                sendMessageToChannel("Sadge, autoplay failed to keep the jams going. Feel free to queue up more " +
                        "music, and report the issue in the support server if this keeps up.")
                cleanupPlayer()
                return
            }
            loadAutoplayTrack(autoplayQueue.poll())
        } else {
            playTrack(next)
        }
    }

    fun cleanupPlayer() {
        val link = getLink()
        link.destroy().block()
        guild.jda.directAudioController.disconnect(guild)
        currentVoiceChannel = null
        trackProvider.clearAll()
        isPaused = false
        GuildVoiceProvider.getInstance().remove(guild.idLong)
        GuildDAO.getInstance().recordTimeInVoice(guild.id, getAge().toInt())
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

    // Gets the age in minutes
    fun getAge() : Long {
        return Instant.now().minusMillis(created.toEpochMilli()).getLong(ChronoField.INSTANT_SECONDS) / 60
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

    fun seek(pos: Long, track: QueuedAudioTrack) {
        if (pos < 0 || pos > track.track.info.length) {
            throw InvalidInputException("Cannot seek to less than 0 or longer than the current track is.")
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
        metricsManager.markTrackPlayed(autoplay, track.track.info.sourceName)
        injectOauth(track.track.info.uri!!)
        getLink().createOrUpdatePlayer()
            .setVolume(volume)
            .setTrack(track.track).subscribe{
                if (lastTextChannel != null && getRepeatMode() != RepeatMode.REPEAT_ONE) {
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

    private fun injectOauth(ident: String) {
        if (oauthConfig != null) {
            // Check for refresh
            if (oauthConfig!!.needsRefresh()) {
                oauthConfig = Oauth2Utils.refreshAccessToken(oauthConfig!!)
                oauthConfigDAO.setOauthConfig(oauthConfig!!)
            }
            LLUtils.injectOauth(oauthConfig!!.accessToken, ident, getLink().node)
        } else {
            findOauth()
        }
    }

    private fun findOauth() {
        if (currentVoiceChannel == null) {return}
        findOauth(currentVoiceChannel!!)
    }

    private fun findOauth(channel: VoiceChannel) {
        if (oauthConfig == null) {
            //  search for a oauth config to use for anyone in channel
            // Refresh if needed.
            for (m in channel.members) {
                var config = oauthConfigDAO.getOauthConfig(m.user.id)
                if (config != null) {
                    if (config.needsRefresh()) {
                        config = Oauth2Utils.refreshAccessToken(config)
                        oauthConfigDAO.setOauthConfig(config)
                    }
                    oauthConfig = config
                }
            }
            if (oauthConfig == null) {
                throw OauthNotEnabledException("No signed in user present. Please login with the `~login` command. " +
                        "This is needed for Vinny to be able to use voice.")
            }
        }
    }

    fun toggleVolumeLock() : Boolean {
        volumeLocked = !volumeLocked
        return volumeLocked
    }

    fun shuffleTracks() {
        trackProvider.shuffleQueue()
    }

    fun removeTrackAtIndex(index: Int): QueuedAudioTrack {
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

    fun autoplayFail() {
        lastTextChannel!!.sendMessage("Failed to load autoplay track, trying to load another.").queue()
        nextTrack(false)
    }

    private fun refreshAutoPlay() {
        if (autoplay) {
            // Refresh if we are on the last track
            if (trackProvider.getQueued().isEmpty()) {
                val np = trackProvider.getNowPlaying()!!
                // If we are autoplaying, only update the queue when at the end.
                if (!np.isAutoplay()) {
                    // If not autoplaying right now, clear the autoplay queue so we refresh it all to whats just finished.
                    autoplayQueue.clear()
                } else if (!autoplayQueue.isEmpty()) {
                    // If we are autoplaying and there is some queue left, no need to refresh till last track.
                    return
                }

                if (np.track.info.sourceName.contains(VinnyConfig.instance().voiceConfig.autoplaySource ?: "NONE")) {
                    autoplayQueue.addAll(AutoplayClient.VideoRequester.getRecommendedVideoIds(np.track.info.identifier))
                } else if (VinnyConfig.instance().voiceConfig.autoplaySearch != null) {
                    // If the current track is not from preferred autoplay provider, then we can do some cool search magic to
                    // transition the preferred platform.
                    autoplayQueue.addAll(AutoplayClient.VideoRequester.getRecommendedVideoIdsSearch(np.track.info.title))
                } else {
                    sendMessageToChannel("Attempted to prep auto play tracks, but autoplay is not supported for " +
                            "that audio source at this time. Sorry, feel free to request it as a feature on our discord server.")
                }
            }
        }
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
            val embed = FormattingUtils.getAudioTrackEmbed(trackProvider.getNowPlaying(), volume, trackProvider.getRepeateMode(), autoplay)
            if (lastMessage.author.id == lastTextChannel!!.jda.selfUser.id) {
                lastMessage.editMessageEmbeds(embed).queue()
            } else {
                lastTextChannel!!.sendMessageEmbeds(embed).queue()
            }
        }
    }
}