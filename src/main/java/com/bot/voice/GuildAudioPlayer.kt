package com.bot.voice

import com.bot.exceptions.UserFacingException
import com.bot.models.TrackLoadContext
import com.bot.preferences.GuildPreferencesManager
import com.bot.utils.FormattingUtils
import com.bot.utils.Logger
import com.bot.utils.RepeatSetting
import com.jagrosh.jdautilities.command.CommandEvent
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import lavalink.client.io.Link
import lavalink.client.io.jda.JdaLavalink
import lavalink.client.player.event.AudioEventAdapterWrapped
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel

class GuildAudioPlayer (
        var lavalink: JdaLavalink,
        private val audioTrackProvider: TrackProvider,
        val guild: Guild,
        var textChannel: TextChannel?,
        val guildPreferencesManager: GuildPreferencesManager,
        val audioPlayerManager: AudioPlayerManager) : AudioEventAdapterWrapped() {

    val player = lavalink.getLink(guild).player
    var nowPlaying: QueuedAudioTrack? = null
    private val logger = Logger(this::class.java.simpleName)
    var repeat = RepeatSetting.NONE

    val trackLoader: TrackLoader

    var volume: Int
        get() = player.volume
        set(volume) {player.volume = volume}

    init {
        player.addListener(this)
        player.volume = guildPreferencesManager.getSettings(guild)!!.getdVolume()

        trackLoader = TrackLoader(audioTrackProvider, audioPlayerManager, this)
    }

    fun nowPlaying() : QueuedAudioTrack {
        return if (player.playingTrack == null && nowPlaying == null)
            audioTrackProvider.peek() else nowPlaying()
    }

    fun isPlaying(): Boolean {
        return player.playingTrack != null && !player.isPaused
    }

    fun isPaused(): Boolean {
        return player.isPaused
    }

    fun currentVoiceChannel(): VoiceChannel? {
        return if (guild.selfMember.voiceState != null) guild.selfMember.voiceState!!.channel
            else null
    }

    fun lastActiveTextChannel(): TextChannel? {
        return textChannel
    }

    fun getPosition(): Long {
        return player.trackPosition
    }

    fun isQueueEmpty() : Boolean {
        return player.playingTrack == null && audioTrackProvider.isEmpty()
    }

    fun allTracks() : List<QueuedAudioTrack> {
        val queue = ArrayList<QueuedAudioTrack>()
        if (nowPlaying != null)
            queue.add(nowPlaying!!)
        queue.addAll(audioTrackProvider.trackQueue.toList())
        return queue
    }

    fun play() {
        if (player.isPaused) {
            player.isPaused = false
        }
        if (player.playingTrack == null) {
            playNextTrack()
        }
    }

    fun setPaused(isPaused: Boolean) {
        player.isPaused = isPaused
        if (!player.isPaused)
            play()
    }

    fun stop() {
        audioTrackProvider.clear()
        stopPlayer()
    }

    fun skipTrack() {
        playNextTrack()
    }

    fun stopPlayer() {
        nowPlaying = null
        player.stopTrack()
    }

    private fun playNextTrack() {
        playTrack(audioTrackProvider.nextTrack())
    }

    private fun playTrack(track: QueuedAudioTrack) {
        nowPlaying = track
        player.playTrack(nowPlaying!!.track)
        // TODO: Notify of new track if enabled
    }

    open fun destroy() {
        stop()
        player.removeListener(this)
        player.link.destroy()
    }

    fun joinChannel(channel: VoiceChannel) {
        if (channel == currentVoiceChannel())
            return

        if (channel.userLimit > 0 && channel.members.size >= channel.userLimit
                && !channel.guild.selfMember.hasPermission(Permission.VOICE_MOVE_OTHERS)) {
            throw UserFacingException("Voice channel is full! Please make some room or give me the " +
                    "VOICE_MOVE_OTHERS permission to bypass the limit.")
        }

        val ll = lavalink.getLink(guild)

        if (ll.state == Link.State.CONNECTED && !currentVoiceChannel()?.members?.contains(guild.selfMember)!!) {
            ll.onDisconnected()
        }

        try {
            ll.connect(channel)
        } catch (e: Exception) {
            logger.severe("Failed to join text channel", e)
        }
    }

    fun leaveChannel() {
        lavalink.getLink(guild).disconnect()
    }

    fun queue(url: String, event: CommandEvent, progress: Message) {
        joinChannel(event.member.voiceState!!.channel!!)

        trackLoader.loadTrack(TrackLoadContext(url, event, progress), event.member)
    }

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) {
        if (endReason == AudioTrackEndReason.FINISHED || endReason == AudioTrackEndReason.STOPPED) {
            playNextTrack()
        } else if (endReason == AudioTrackEndReason.LOAD_FAILED) {
            // TODO: Notify of load failure
            playNextTrack()
        } else {
            if (endReason == null) {
                logger.warning("Track ended with a null reason")
                return
            }
            logger.warning("A track ended playinh with an unexpected reason: " + endReason.name)
        }
    }

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack, exception: FriendlyException?) {
        logger.severe("Lavaplayer encountered an error while playing " + track.identifier, exception)
    }

    override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack, thresholdMs: Long) {
        logger.severe("Track stuck while playing " + track.identifier)
    }

    private fun trackAnnounceEnabled(): Boolean {
        return true //TODO
    }

    fun announceNewTrack(track: QueuedAudioTrack) {
        if (repeat == RepeatSetting.ONE || !trackAnnounceEnabled() || isPaused())
            return

        // Refresh the last channel to make sure we dont have a stale one
        textChannel = guild.jda.getTextChannelById(textChannel!!.id)
        textChannel?.sendMessage(FormattingUtils.getAudioTrackEmbed(track, volume))?.queue()
    }

    fun announceError(t: Throwable) {
        logger.severe("Failed to load track", Exception(t))
        textChannel = guild.jda.getTextChannelById(textChannel!!.id)
        textChannel?.sendMessage("Something went wrong loading the track: ${t.message}}")?.queue()
    }
}