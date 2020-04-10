package com.bot.voice

import com.bot.preferences.GuildPreferencesManager
import com.bot.utils.Logger
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import lavalink.client.io.jda.JdaLavalink
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import java.util.concurrent.ConcurrentHashMap

class AudioPlayerController(val guildPreferencesManager: GuildPreferencesManager,
                            val audioPlayerManager: AudioPlayerManager,
                            val lavalink: JdaLavalink) {
    private val logger = Logger(AudioPlayerController::class.simpleName)

    private val players = ConcurrentHashMap<String, GuildAudioPlayer>()
    private val iteratorLock = Any()

    fun getOrCreate(guild: Guild, channel: TextChannel): GuildAudioPlayer {
        return players.computeIfAbsent(guild.id) {
            GuildAudioPlayer(lavalink, TrackProvider(), guild, channel, guildPreferencesManager, audioPlayerManager)
        }
    }

    fun get(guild: Guild): GuildAudioPlayer? {
        return players[guild.id]
    }

    fun destroyPlayer(guild: Guild) {
        val player = get(guild)
        player?.destroy()
        players.remove(guild.id)
    }

    fun size(): Int{
        return players.size
    }

    fun playingCount(): Long {
        synchronized(iteratorLock) {
            return players.values.stream()
                    .filter {it.isPlaying()}
                    .count()
        }
    }
}