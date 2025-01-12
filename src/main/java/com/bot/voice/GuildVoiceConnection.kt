package com.bot.voice

import com.bot.db.GuildDAO
import com.bot.db.OauthConfigDAO
import com.bot.db.models.OauthConfig
import com.bot.db.models.ResumeAudioGuild
import com.bot.exceptions.InvalidInputException
import com.bot.exceptions.NotInVoiceException
import com.bot.exceptions.OauthNotEnabledException
import com.bot.exceptions.UserExposableException
import com.bot.i18n.Translator
import com.bot.metrics.MetricsManager
import com.bot.models.enums.RepeatMode
import com.bot.utils.*
import com.bot.commands.control.CommandControlEvent
import com.jagrosh.jdautilities.menu.OrderedMenu.Builder
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.LinkState
import dev.arbjerg.lavalink.client.event.TrackEndEvent
import dev.arbjerg.lavalink.client.event.TrackExceptionEvent
import dev.arbjerg.lavalink.client.loadbalancing.VoiceRegion
import dev.arbjerg.lavalink.client.player.Track
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.apache.log4j.Logger
import reactor.kotlin.core.publisher.toMono
import java.time.Instant
import java.time.temporal.ChronoField
import java.util.*
import kotlin.collections.HashSet

class GuildVoiceConnection(val guild: Guild) {
    val logger = Logger.getLogger(this::class.java.name)
    val translator = Translator.getInstance()
    val lavalink: LavaLinkClient = LavaLinkClient.getInstance()
    val metricsManager = MetricsManager.instance!!
    private val trackProvider = TrackProvider()
    private val autoplayQueue = LinkedList<String>()
    private val failedLoadedTracks = HashSet<String>()
    var lastTextChannel: MessageChannel? = null
    var nowPlayingMessage: Message? = null
    private var isPaused = false
    private var volume = GuildDAO.getInstance().getGuildById(guild.id).volume ?: 100
    var autoplay = GuildDAO.getInstance().isGuildPremium(guild.id)
    var volumeLocked = false
    private var created = Instant.now()
    var region: VoiceRegion? = null
    var oauthConfig: OauthConfig? = null
    var oauthConfigDAO = OauthConfigDAO.getInstance()
    var failedAttempt = 0

    // Used for ignoring inject creds
    val soundcloudRegex = Regex(
            """https://soundcloud\.com/(?:[a-z0-9]+/)?(?:[a-z0-9]+/(?:[a-z0-9]+|sets/[a-z0-9]+))?"""
    )

    fun setPaused(pause: Boolean) {
        lavalink.getLink(guild.idLong).getPlayer()
            .flatMap { it.setPaused(pause).toMono() }.subscribe{ this.isPaused = it.paused }
        sendNowPlayingUpdate()
    }

    fun getPaused() : Boolean {
        return isPaused
    }

    fun joinChannel(controlEvent: CommandControlEvent) {
        val toJoin = controlEvent.getMember().voiceState?.channel
            ?: throw NotInVoiceException("You are not in a voice channel! Please join one to use this command.")
        try {
            joinChannel(toJoin.asVoiceChannel())
        } catch (e: UserExposableException) {
            throw e
        } catch (e: Exception) {
            controlEvent.replyWarning("Failed to join voice channel: ${e.message}")
            cleanupPlayer()
            throw e
        }
    }

    private fun joinChannel(channel: VoiceChannel) {
        if (channel == channel.guild.selfMember.voiceState?.channel && isConnected()) {
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

        findOauth(channel)

        try {
            channel.jda.directAudioController.connect(channel)
            var checks = 0
            val timeout = 10
            // This wait will slow down execution until we receive the event back from discord
            // indicating successful connection. This will allow us to loadbalance over region.
            // Without the region info (getLink() before we get that event back) will nullify regional loadbalancing.
            while (true) {
                if (checks >= timeout) {
                    break
                }
                if (region != null) {
                    break
                }
                checks++
                Thread.sleep(200)
            }
        } catch (e: Exception) {
            logger.error("Failed to join voice channel $channel", e)
            throw e
        }
        GuildDAO.getInstance().updateLastVoiceConnectTime(guild.id)
    }

    fun loadTrack(toLoad: String, controlEvent: CommandControlEvent) {
        joinChannel(controlEvent)
        val link = getLink()

        injectOauth(toLoad)
        metricsManager.markTrackLoaded()
        link.loadItem(toLoad).subscribe(LLLoadHandler(this, controlEvent))
        lastTextChannel = controlEvent.getChannel()
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
    fun queuePlaylistTrack(queuedTrack: QueuedAudioTrack?, controlEvent: CommandControlEvent, loadingMessage: Message,
                           tracks: List<String>, index: Int, failedCount: Int) {
        val link = getLink()

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
            PlaylistLLLoadHandler(this, controlEvent, loadingMessage, tracks, newIndex, failedCount))
    }


    fun queuePlaylist(tracks: List<String>, controlEvent: CommandControlEvent, loadingMessage: Message) {
        joinChannel(controlEvent)
        val link = getLink()

        injectOauth(tracks[0])
        metricsManager.markTrackLoaded()
        link.loadItem(tracks[0]).subscribe(PlaylistLLLoadHandler(this, controlEvent, loadingMessage, tracks, 0, 0))
        lastTextChannel = controlEvent.getChannel()
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

    fun resumeAudioAfterReboot(resumeSetup: ResumeAudioGuild, channel: VoiceChannel) {
        volume = resumeSetup.volume
        volumeLocked = resumeSetup.volumeLocked
        try {
            joinChannel(channel)
        } catch (e: Exception) {
            if (e is OauthNotEnabledException) {
                sendMessageToChannel(e.message!!)
                return
            }
        }

        val link = getLink()

        lastTextChannel!!.sendMessage(translator.translate("VOICE_REBOOT_RESUME", guild.locale.locale)).queue()
        val loadingMessage = lastTextChannel!!.sendMessage("Loading previous queue...").complete()
        injectOauth(resumeSetup.tracks[0].trackUrl)
        metricsManager.markTrackLoaded()
        link.loadItem(resumeSetup.tracks[0].trackUrl).subscribe(ResumeLLLoadHandler(this, loadingMessage, resumeSetup, 0, 0))
    }

    fun searchForTrack(search: String, controlEvent: CommandControlEvent, message: Message, builder: Builder) {
        joinChannel(controlEvent)
        val link = getLink()

        injectOauth(search)
        metricsManager.markTrackLoaded()
        link.loadItem(search).subscribe(SearchLLLoadHandler(this, controlEvent, message, builder))
        lastTextChannel = controlEvent.getChannel()
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
                sendMessageToChannel(translator.translate("VOICE_OUT_OF_TRACKS", guild.locale.locale))
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
            // Prevent repeating failed loads and plays
            if (trackProvider.getRepeateMode() != RepeatMode.REPEAT_NONE) {
                if (failedLoadedTracks.contains(next.track.info.uri)) {
                    failedAttempt += 1
                    if (failedAttempt >= 5) {
                        sendMessageToChannel(translator.translate("VOICE_MANY_FAILED_TRACKS", guild.locale.locale))
                        cleanupPlayer()
                        return
                    }
                    sendMessageToChannel("Skipping track load that I have previously failed: ${next.track.info.title}")
                    nextTrack(skipping)
                }
            } else {
                failedAttempt = 0
            }
            playTrack(next)
        }
    }

    fun cleanupPlayer() {
        val link = getLink()
        link.destroy().block()
        guild.jda.directAudioController.disconnect(guild)
        trackProvider.clearAll()
        isPaused = false
        nowPlayingMessage?.delete()?.queue()
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
        sendNowPlayingUpdate(translator.translate("VOICE_REPEAT_MODE_CHANGE", guild.locale.locale, mode.name))
    }

    fun nextRepeatMode() {
        when (getRepeatMode()) {
            RepeatMode.REPEAT_ALL -> setRepeatMode(RepeatMode.REPEAT_ONE)
            RepeatMode.REPEAT_ONE -> setRepeatMode(RepeatMode.REPEAT_NONE)
            RepeatMode.REPEAT_NONE -> setRepeatMode(RepeatMode.REPEAT_ALL)
        }
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
            throw InvalidInputException(translator.translate("VOICE_VOLUME_LOCKED", guild.locale.locale))
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
        return lavalink.getLink(guild.idLong, region)
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
        if (soundcloudRegex.matches(ident)) {
            // can skip
            return
        }

        if (oauthConfig != null) {
            // Check for refresh
            if (oauthConfig!!.needsRefresh()) {
                updateOauthConfig(Oauth2Utils.refreshAccessToken(oauthConfig!!))
            }
        } else {
            findOauth()
        }
        getLink().node
        oauthConfig!!.accessToken
        LLUtils.injectOauth(oauthConfig!!.accessToken, ident, getLink().node)
    }

    private fun findOauth() {
        if (guild.selfMember.voiceState?.channel == null) {return}
        findOauth(guild.selfMember.voiceState!!.channel!!.asVoiceChannel())
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
                    }
                    updateOauthConfig(config)
                }
            }
            if (oauthConfig == null) {
                throw OauthNotEnabledException(translator.translate("VOICE_NO_OAUTH_FOUND", guild.locale.locale))
            }
        }
    }

    fun toggleVolumeLock() : Boolean {
        volumeLocked = !volumeLocked
        return volumeLocked
    }

    fun shuffleTracks() {
        trackProvider.shuffleQueue()
        sendNowPlayingUpdate(translator.translate("VOICE_TRACKS_SHUFFLED", guild.locale.locale))
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

    fun markFailedLoad(failed: Track, exceptionEvent: TrackExceptionEvent) {
        // Mark a failed load track so we dont infinite loop autoplay it
        failedLoadedTracks.add(failed.info.uri!!)
        sendMessageToChannel("Failed to load track `${failed.info.title}`. The error I received was: `${exceptionEvent.exception.message}`.")
    }

    fun sendMessageToChannel(msg: String) {
        try {
            lastTextChannel!!.sendMessage(msg).queue()
        } catch (e: Exception) {
            logger.error("Guild voice: could not send message to channel", e)
        }
    }

    fun updateOauthConfig(config: OauthConfig) {
        oauthConfig = config
        failedLoadedTracks.clear()
    }

    private fun sendNowPlayingUpdate() {
        sendNowPlayingUpdate("")
    }

    private fun sendNowPlayingUpdate(msg: String) {
        // If we sent the last message in the channel then just edit it
        lastTextChannel!!.history.retrievePast(1).queue { m ->
            val lastMessage: Message = m[0]
            val embed = FormattingUtils.getAudioTrackEmbed(trackProvider.getNowPlaying(), volume, trackProvider.getRepeateMode(), autoplay, getLink().node.name)

            if (lastMessage.author.id == lastTextChannel!!.jda.selfUser.id) {
                lastMessage.editMessageEmbeds(embed).setActionRow(actionBar()).setContent(msg).queue { this.nowPlayingMessage = it }
            } else {
                // Delete our last playing message and put a new one at the bottom
                if (this.nowPlayingMessage != null) {
                    this.nowPlayingMessage!!.delete().queue({}, { println(it) })
                }
                lastTextChannel!!.sendMessageEmbeds(embed)
                    .addActionRow(actionBar())
                    .addContent(msg)
                    .queue { this.nowPlayingMessage = it }
            }
        }
    }

    private fun actionBar() : MutableCollection<ItemComponent> {
        // Paused state shows play button and vice versa
        val playPauseButton = if (isPaused) Button.success("voicecontrol-playpause", ConstantEmojis.playEmoji)
            else Button.secondary("voicecontrol-playpause", ConstantEmojis.pauseEmoji)

        // Create the buttons using the emoji variables
        val stopButton = Button.danger("voicecontrol-stop", ConstantEmojis.stopEmoji)
        val nextButton = Button.primary("voicecontrol-next", ConstantEmojis.nextEmoji)
        val shuffleButton = Button.primary("voicecontrol-shuffle", ConstantEmojis.shuffleEmoji)
        val repeatButton = Button.primary("voicecontrol-repeat", getRepeatMode().emoji)

        return mutableSetOf(stopButton, playPauseButton, nextButton, shuffleButton, repeatButton)
    }

    fun getCurrentVoiceChannel() : VoiceChannel? {
        return guild.selfMember.voiceState!!.channel?.asVoiceChannel()
    }
}