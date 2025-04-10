package com.bot.voice

import com.bot.commands.control.CommandControlEvent
import com.bot.db.GuildDAO
import com.bot.db.OauthConfigDAO
import com.bot.db.models.OauthConfig
import com.bot.db.models.ResumeAudioGuild
import com.bot.exceptions.InvalidInputException
import com.bot.exceptions.NotInVoiceException
import com.bot.exceptions.UserExposableException
import com.bot.exceptions.newstyle.OauthNotEnabledException
import com.bot.i18n.Translator
import com.bot.metrics.MetricsManager
import com.bot.models.enums.RepeatMode
import com.bot.utils.*
import com.bot.voice.radio.*
import com.jagrosh.jdautilities.menu.OrderedMenu.Builder
import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.LinkState
import dev.arbjerg.lavalink.client.event.TrackEndEvent
import dev.arbjerg.lavalink.client.event.TrackExceptionEvent
import dev.arbjerg.lavalink.client.loadbalancing.VoiceRegion
import dev.arbjerg.lavalink.client.player.Track
import dev.arbjerg.lavalink.internal.error.RestException
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

class GuildVoiceConnection(val guild: Guild) {
    val logger = Logger.getLogger(this::class.java.name)
    val translator = Translator.getInstance()
    val lavalink: LavaLinkClient = LavaLinkClient.getInstance()
    val metricsManager = MetricsManager.instance!!
    private val trackProvider = TrackProvider()
    private val autoplayQueue = LinkedList<String>()
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
    val loginReqRegex = Regex(VinnyConfig.instance().voiceConfig.loginReqRegex)
    val loginReqProvider = VinnyConfig.instance().voiceConfig.loginSearchProvider
    var radioStation: LofiRadioStation? = null

    fun setPaused(pause: Boolean) {
        lavalink.getLink(guild.idLong).getPlayer()
            .flatMap { it.setPaused(pause).toMono() }.subscribe{ this.isPaused = it.paused }
        sendNowPlayingUpdate()
    }

    fun getPaused() : Boolean {
        return isPaused
    }

    fun setRadio(id: String, controlEvent: CommandControlEvent) {
        joinChannel(controlEvent)
        lastTextChannel = controlEvent.getChannel()
        setRadio(LofiRadioService.getStation(id)!!)
    }

    fun setRadio(station: LofiRadioStation) {
        // Clear tracks for now, maybe we can hold them if they exist
        trackProvider.clearAll()
        radioStation = station
        loadRadioTrack()
    }

    fun loadRadioTrack() {
        val toPlay = radioStation!!.getNowPlaying()
        getLink().loadItem("lofiradio:${radioStation!!.id}").subscribe(LLLRadioHandler(this, toPlay))
    }

    fun isRadio(): Boolean {
        return radioStation != null
    }

    fun stopRadio() {
        if (isRadio()) {
            radioStation = null
            trackProvider.clearAll()
        }
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

    fun joinChannel(channel: VoiceChannel) {
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

        checkOauth(toLoad)
        metricsManager.markTrackLoaded()
        link.loadItem(toLoad).subscribe(LLLoadHandler(this, controlEvent))
        lastTextChannel = controlEvent.getChannel()
    }

    private fun loadAutoplayTrack(toLoad: String) {
        val link = getLink()
        checkOauth(toLoad)
        metricsManager.markTrackLoaded()
        link.loadItem(toLoad).subscribe(AutoplayLoadHandler(this))
    }

    fun queueTrack(track: QueuedAudioTrack) {
        stopRadio()
        trackProvider.addTrack(track)
        if (trackProvider.getNowPlaying() == track) {
            playTrack(track)
        } else {
            lastTextChannel!!.sendMessage("Queued up `${track.track.info.title}`.").queue()
        }
    }

    fun queueLoadedPlaylist(tracks: List<QueuedAudioTrack>) {
        stopRadio()
        for (track in tracks) {
            trackProvider.addTrack(track)
            if (trackProvider.getNowPlaying() == track) {
                playTrack(track)
            }
        }
        lastTextChannel!!.sendMessage("Queued up ${tracks.size} tracks.").queue()
    }

    // Queue up track from playlist and then start load if there is a next track
    fun queuePlaylistTrack(queuedTrack: QueuedAudioTrack?, controlEvent: CommandControlEvent, loadingMessage: Message,
                           tracks: List<String>, index: Int, failedCount: Int) {
        val link = getLink()

        val newIndex = index+1
        updateLoadingMessage(loadingMessage, tracks, newIndex, failedCount)
        stopRadio()
        if (queuedTrack != null) {
            if (trackProvider.getNowPlaying() == null) {
                playTrack(queuedTrack)
            }
            trackProvider.addTrack(queuedTrack)
        }
        if (newIndex == tracks.size) {
            return
        }
        checkOauth(tracks[newIndex])
        metricsManager.markTrackLoaded()
        link.loadItem(tracks[newIndex]).subscribe(
            PlaylistLLLoadHandler(this, controlEvent, loadingMessage, tracks, newIndex, failedCount))
    }


    fun queuePlaylist(tracks: List<String>, controlEvent: CommandControlEvent, loadingMessage: Message) {
        joinChannel(controlEvent)
        stopRadio()
        val link = getLink()

        checkOauth(tracks[0])
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
                playTrack(queuedTrack) {seek(resumeSetup.tracks[index].position, queuedTrack)}
            }
            trackProvider.addTrack(queuedTrack)
        }
        if (newIndex == resumeSetup.tracks.size) {
            return
        }
        checkOauth(resumeSetup.tracks[newIndex].trackUrl)
        metricsManager.markTrackLoaded()
        link.loadItem(resumeSetup.tracks[newIndex].trackUrl).subscribe(
            ResumeLLLoadHandler(this, loadingMessage, resumeSetup, newIndex, failedCount))
    }

    fun queueRadioTrack(track: RadioQueuedAudioTrack) {
        trackProvider.addTrack(track)
        if (trackProvider.getNowPlaying() == track) {
            playTrack(track) {seek(radioStation!!.getNowPlaying().getCurrentTime(), track)}
        } else {
            lastTextChannel!!.sendMessage("Queued up `${track.getTitle()}`.").queue()
        }
    }

    fun resumeAudioAfterReboot(resumeSetup: ResumeAudioGuild, channel: VoiceChannel) {
        volume = resumeSetup.volume
        volumeLocked = resumeSetup.volumeLocked
        if (resumeSetup.oauth != null) {
            oauthConfig = oauthConfigDAO.getOauthConfig(resumeSetup.oauth)
        }
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
        checkOauth(resumeSetup.tracks[0].trackUrl)
        metricsManager.markTrackLoaded()
        link.loadItem(resumeSetup.tracks[0].trackUrl).subscribe(ResumeLLLoadHandler(this, loadingMessage, resumeSetup, 0, 0))
    }

    fun searchForTrack(search: String, controlEvent: CommandControlEvent, message: Message, builder: Builder) {
        joinChannel(controlEvent)
        val link = getLink()

        checkOauth(search)
        metricsManager.markTrackLoaded()
        link.loadItem(search).subscribe(SearchLLLoadHandler(this, controlEvent, message, builder))
        lastTextChannel = controlEvent.getChannel()
    }


    fun onTrackEnd(event: TrackEndEvent) {
        val endReason = event.endReason
        metricsManager.markTrackEnd(endReason.name, endReason.mayStartNext)

        failedAttempt = if (endReason.name == "LOAD_FAILED") failedAttempt + 1 else 0

        if (failedAttempt >= 5) {
            sendMessageToChannel(translator.translate("VOICE_MANY_FAILED_TRACKS", guild.locale.locale))
            cleanupPlayer()
            return
        }

        if (endReason.mayStartNext) {
            try {
                nextTrack(false)
            } catch (u: UserExposableException) {
                sendMessageToChannel(u.message ?: "Unknown error")
            }
        }
    }

    fun nextTrack(skipping: Boolean) {
        // Make sure autoplay is populated before going to next track
        refreshAutoPlay()

        val next = trackProvider.nextTrack(skipping)
        if (next == null) {
            if (isRadio()) {
                loadRadioTrack()
            } else if (autoplay) {
                // Autoplay failed to load tracks and we have no now playing. Fail softly.
                if (autoplayQueue.isEmpty()) {
                    sendMessageToChannel("Sadge, autoplay failed to keep the jams going. Feel free to queue up more " +
                            "music, and report the issue in the support server if this keeps up.")
                    cleanupPlayer()
                    return
                }
                loadAutoplayTrack(autoplayQueue.poll())
            } else {
                sendMessageToChannel(translator.translate("VOICE_OUT_OF_TRACKS", guild.locale.locale))
                cleanupPlayer()
                return
            }

        } else {
            playTrack(next)
        }
    }

    // TODO: Move this func call path to provider remove func.
    fun cleanupPlayer() {
        try {
            val link = getLink()
            link.destroy().block()
        } catch (e: RestException) {
            // Likely session not found on LL, so skip and continue cleanup
        }
        guild.jda.directAudioController.disconnect(guild)
        trackProvider.clearAll()
        isPaused = false
        nowPlayingMessage?.delete()?.queue()
        GuildVoiceProvider.getInstance().remove(guild.idLong)
        GuildDAO.getInstance().recordTimeInVoice(guild.id, getAge().toInt())
        stopRadio()
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

    fun seek(pos: Long, track: BaseAudioTrack) {
        if (pos < 0 || pos > track.track.info.length) {
            throw InvalidInputException("Cannot seek to less than 0 or longer than the current track is.")
        }
        getLink().createOrUpdatePlayer().setPosition(pos).block()
    }

    fun nowPlaying() : BaseAudioTrack? {
        return trackProvider.getNowPlaying()
    }

    fun getPosition() : Long? {
        return getLink().getPlayer().block()?.position
    }

    fun getQueuedTracks() : List<BaseAudioTrack> {
        return trackProvider.getQueued()
    }

    private fun getLink() : Link {
        return lavalink.getLink(guild.idLong, region)
    }

    private fun playTrack(track: BaseAudioTrack, after: (() -> Unit)? = null) {
        metricsManager.markTrackPlayed(autoplay, track.track.info.sourceName)
        checkOauth(track.track.info.uri!!)
        if (isInjectRequired(track.track.info.uri!!)) {
            track.track.setUserData(mapOf(Pair("oauth-token", oauthConfig!!.accessToken)))
        }
        getLink().createOrUpdatePlayer()
            .setVolume(volume)
            .setTrack(track.track)
            .subscribe {
                if (lastTextChannel != null && getRepeatMode() != RepeatMode.REPEAT_ONE) {
                    sendNowPlayingUpdate()
                }
                after?.invoke() // Run the function if it's provided
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

    private fun checkOauth(ident: String) {
        if (!isInjectRequired(ident)) {
            // can skip
            return
        }

        if (oauthConfig != null) {
            // Check for refresh
            if (oauthConfig!!.needsRefresh()) {
                updateOauthConfig(Oauth2Utils.refreshAccessToken(oauthConfig!!))
            }
        } else {
            oauthRequired()
        }
    }

    private fun isInjectRequired(ident: String): Boolean {
        return ident.contains(loginReqProvider) || ident.matches(loginReqRegex)
    }

    private fun oauthRequired() {
        findOauth()
        if (oauthConfig == null) {
            throw OauthNotEnabledException("VOICE_NO_OAUTH_FOUND")
        }
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

    fun removeTrackAtIndex(index: Int): BaseAudioTrack {
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

    fun removeTrackByURLOrSearch(url: String): BaseAudioTrack? {
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
    }

    private fun sendNowPlayingUpdate() {
        sendNowPlayingUpdate("")
    }

    private fun sendNowPlayingUpdate(msg: String) {
        // If we sent the last message in the channel then just edit it
        lastTextChannel!!.history.retrievePast(1).queue { m ->
            val lastMessage: Message = m[0]
            val embed = FormattingUtils.getAudioTrackEmbed(trackProvider.getNowPlaying(), volume, trackProvider.getRepeateMode(), autoplay, getLink().node.name)

            if (lastMessage.author.id == lastTextChannel!!.jda.selfUser.id && lastMessage.embeds.isNotEmpty()) {
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
        val stopButton = Button.danger("voicecontrol-stop", ConstantEmojis.stopEmoji)
        val items = mutableListOf(playPauseButton, stopButton)

        if (!isRadio()) {
            val nextButton = Button.primary("voicecontrol-next", ConstantEmojis.nextEmoji)
            items.add(nextButton)
            val shuffleButton = Button.primary("voicecontrol-shuffle", ConstantEmojis.shuffleEmoji)
            items.add(shuffleButton)
            val repeatButton = Button.primary("voicecontrol-repeat", getRepeatMode().emoji)
            items.add(repeatButton)
        }

        return items.toMutableList()
    }

    fun getCurrentVoiceChannel() : VoiceChannel? {
        return guild.selfMember.voiceState!!.channel?.asVoiceChannel()
    }

    fun moveTrack(trackPos: Int, newPos: Int) {
        trackProvider.moveTrack(trackPos, newPos)
    }

    fun reconnect() {
        val pos = getPosition()?: 0
        val nowPlaying = nowPlaying()
        getLink().destroy().block()
        if (nowPlaying != null) {
            playTrack(nowPlaying) { seek(pos, nowPlaying) }
        } else {
            // Force a getlink to be safe
            getLink()
        }
    }
}