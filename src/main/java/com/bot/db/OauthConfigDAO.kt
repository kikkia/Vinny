package com.bot.db

import com.bot.db.models.OauthConfig
import com.bot.db.models.OauthConfig.Companion.mapSetToOauthConfig
import com.bot.utils.DbHelpers.close
import com.bot.utils.Logger
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.logging.Level

class OauthConfigDAO {
    private val LOGGER = Logger(OauthConfigDAO::class.java.name)

    private var write: HikariDataSource = ConnectionPool.getDataSource()

    companion object {
        @Volatile private var instance: OauthConfigDAO? = null
        fun getInstance(): OauthConfigDAO =
                instance ?: synchronized(this) {
                    instance ?: OauthConfigDAO().also { instance = it }
                }
    }

    fun getOauthConfig(id: String): OauthConfig? {
        try {
            write.connection.use { connection ->
                val query = "SELECT * FROM `user_oauth_config` WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, id)
                    statement.executeQuery().use { set ->
                        if (set.next()) {
                            return mapSetToOauthConfig(set)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            LOGGER.log(Level.SEVERE, "Failed to get oauth config for: $id")
        }
        return null
    }

    fun getTotalOauthConfigs(): Int {
        val query = "SELECT count(*) FROM user_oauth_config"
        var connection: Connection? = null
        var statement: PreparedStatement? = null
        try {
            connection = write.connection
            statement = connection.prepareStatement(query)
            val set = statement.executeQuery()
            if (set.next()) {
                return set.getInt(1)
            }
        } catch (e: SQLException) {
            LOGGER.log(Level.SEVERE, "Failed to get total oauthconfigs: " + e.message)
        } finally {
            close(statement, null, connection)
        }
        return -1
    }

    fun setOauthConfig(config: OauthConfig) {
        try {
            write.connection.use { connection ->
                val query = """
                    INSERT INTO `user_oauth_config` (`id`, `refresh_token`, `access_token`, `token_type`, `expiry`) 
                    VALUES (?, ?, ?, ?, ?) 
                        ON DUPLICATE KEY UPDATE 
                            `refresh_token` = VALUES(`refresh_token`),
                            `access_token` = VALUES(`access_token`),
                            `token_type` = VALUES(`token_type`),
                            `expiry` = VALUES(`expiry`);
                    """
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, config.userId)
                    statement.setString(2, config.refreshToken)
                    statement.setString(3, config.accessToken)
                    statement.setString(4, config.tokenType)
                    statement.setLong(5, config.expiry.epochSecond)
                    statement.execute()
                }
            }
        } catch (e: SQLException) {
           LOGGER.log(Level.SEVERE, "Failed to set oauth config for: " + config.userId)
        }
    }

    fun removeConfig(id: String) {
        try {
            write.connection.use { connection ->
                val query = "DELETE FROM `user_oauth_config` WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, id)
                    statement.executeQuery()
                }
            }
        } catch (e: SQLException) {
            LOGGER.log(Level.SEVERE, "Failed to delete oauth config for: $id")
        }
    }
}