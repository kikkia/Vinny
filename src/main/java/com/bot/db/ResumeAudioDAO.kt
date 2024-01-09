package com.bot.db

import com.bot.db.models.ResumeAudioGuild
import com.bot.db.models.ResumeAudioTrack
import com.bot.utils.Logger
import com.zaxxer.hikari.HikariDataSource
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.LinkedList

class ResumeAudioDAO {
    private val LOGGER = Logger(ScheduledCommandDAO::class.java.name)

    private var write: HikariDataSource = ConnectionPool.getDataSource()

    companion object {
        @Volatile private var instance: ResumeAudioDAO? = null
        fun getInstance(): ResumeAudioDAO =
            instance ?: synchronized(this) {
                instance ?: ResumeAudioDAO().also { instance = it }
            }
    }

    fun storeResumeGuild(guildId: String, vcId: String, tcId: String, volume: Int, volumeLock: Boolean, tracks: List<ResumeAudioTrack>) {
        val query =  "INSERT INTO `resume_audio_guild` (`id`, `voice_channel_id`, `text_channel_id`, `volume`, `volume_locked`) VALUES (?, ?, ?, ?, ?)"
        write.connection.use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setString(1, guildId)
                preparedStatement.setString(2, vcId)
                preparedStatement.setString(3, tcId)
                preparedStatement.setInt(4, volume)
                preparedStatement.setBoolean(5, volumeLock)
                preparedStatement.execute()
            }
        }

        for (i in tracks.indices) {
            storeResumeTrack(tracks[i], guildId, i)
        }
    }

    @Throws(SQLException::class)
    fun storeResumeTrack(track: ResumeAudioTrack, guildId: String, index: Int) {
        val query =
            "INSERT INTO resume_audio_track(resume_guild, track_url, requester_name, requester_id, track_position, track_index) VALUES (?,?,?,?,?,?)"
        write.connection.use { connection ->
            connection.prepareStatement(query).use { preparedStatement ->
                preparedStatement.setString(1, guildId)
                preparedStatement.setString(2, track.trackUrl)
                preparedStatement.setString(3, track.requesterName)
                preparedStatement.setLong(4, track.requesterId)
                preparedStatement.setLong(5, track.position)
                preparedStatement.setInt(6, index)
                preparedStatement.execute()
            }
        }
    }

    private fun getAllForGuildId(guildId: String) : List<ResumeAudioTrack> {
        val query =
            "SELECT resume_guild, track_url, requester_name, requester_id, track_position, track_index FROM resume_audio_track WHERE resume_guild = ? order by track_index ASC"
        write.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, guildId)
                return getTracks(statement)
            }
        }
    }

    fun deleteAllForGuildId(guildId: String) {
        var query = "DELETE FROM resume_audio_track WHERE resume_guild = ?"
        write.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, guildId)
                statement.executeUpdate()
            }
        }

        query = "DELETE FROM resume_audio_guild WHERE id = ?"
        write.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, guildId)
                statement.executeUpdate()
            }
        }
    }

    fun getResumeGuild(guildId: String) : ResumeAudioGuild {
        val query = "SELECT * from resume_audio_guild where id = ?;"
        val tracks = getAllForGuildId(guildId)
        write.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, guildId)
                val result = statement.executeQuery()
                result.next()
                return ResumeAudioGuild(guildId,
                    result.getString("voice_channel_id"),
                    result.getString("text_channel_id"),
                    result.getInt("volume"),
                    result.getBoolean("volume_locked"),
                    tracks)
            }
        }
    }

    fun getAllResumeGuilds() : List<String> {
        val query = "SELECT id from resume_audio_guild;"
        val guilds = LinkedList<String>()

        write.connection.use { connection ->
            connection.prepareStatement(query).use { statement ->
                val results = statement.executeQuery()
                while (results.next()) {
                    guilds.add(results.getString(1))
                }
                return guilds
            }
        }
    }

    @Throws(SQLException::class)
    private fun getTracks(statement: PreparedStatement): List<ResumeAudioTrack> {
        val tracks: MutableList<ResumeAudioTrack> = ArrayList()
        statement.executeQuery().use { set ->
            while (set.next()) {
                tracks.add(
                    ResumeAudioTrack(
                        set.getString("track_url"),
                        set.getLong("track_position"),
                        set.getString("requester_name"),
                        set.getLong("requester_id")
                    )
                )
            }
            return tracks
        }
    }

}