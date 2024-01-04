package com.bot.voice

import net.dv8tion.jda.api.entities.Guild
import java.util.concurrent.ConcurrentHashMap

class GuildVoiceProvider {
    // TODO: LRU CACHE?
    private val guildVoiceConnections: ConcurrentHashMap<Long, GuildVoiceConnection> = ConcurrentHashMap()

    fun getGuildVoiceConnection(guild: Guild): GuildVoiceConnection {
        return guildVoiceConnections.computeIfAbsent(guild.idLong) {
            GuildVoiceConnection(guild)
        }
    }

    fun getGuildVoiceConnection(id: Long): GuildVoiceConnection? {
        return guildVoiceConnections[id]
    }

    companion object {
        @Volatile private var instance: GuildVoiceProvider? = null
        fun getInstance(): GuildVoiceProvider =
            instance ?: synchronized(this) {
                instance ?: GuildVoiceProvider().also { instance = it }
            }
    }
}