package com.bot.tasks

import com.bot.db.OauthConfigDAO
import com.bot.db.models.OauthConfig
import com.bot.metrics.MetricsManager
import com.bot.utils.VinnyConfig
import com.bot.voice.GuildVoiceProvider
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.time.Instant

class OauthCheckerTask(private val deviceCode: String,
                       private val interval: Long,
                       val commandEvent: CommandEvent): Thread() {
    val client: CloseableHttpClient = HttpClients.createDefault()
    private val guildProvider = GuildVoiceProvider.getInstance()
    private val oauthDAO = OauthConfigDAO.getInstance()
    private val oauthProperties = VinnyConfig.instance().voiceConfig.oauthConfig
    private val log = LoggerFactory.getLogger(OauthCheckerTask::class.java)
    private val metricsManager = MetricsManager.instance
    private val start = Instant.now()


    override fun run() {
        while (true) {
            // 10 min timeout
            if (start.isBefore(Instant.now().minusSeconds(600))) {
                commandEvent.replyError("Linking to account failed. It looks like it's timed out. Please try again.")
                return
            }
            val response = poll()
            if (response.errorMessage.isNotEmpty()) {
                when(response.errorMessage) {
                    "authorization_pending", "slow_down" -> {
                        sleep(interval)
                        continue
                    }
                    "expired_token" -> {
                        commandEvent.replyError("Linking to account failed. It looks like it's timed out. Please try again.")
                    }

                    "access_denied" -> {
                        commandEvent.replyError("Linking to account was denied. :( Please try again.")
                    }

                    else -> {
                        commandEvent.replyError("An unknown error occurred while checking for signin completion.")
                        log.error("Error with Oauth2 flow: {}", response.errorMessage)
                        // Only one that may be our fault, not timeout or deny
                        metricsManager!!.markOauthComplete(false)
                    }
                }
                client.close()
                return
            }
            val conn = guildProvider.getGuildVoiceConnection(commandEvent.guild)
            val newConfig = OauthConfig(commandEvent.author.id, response.refreshToken, response.accessToken, response.tokenType, response.tokenExpires, true)
            conn.updateOauthConfig(newConfig)
            oauthDAO.setOauthConfig(newConfig)
            commandEvent.replySuccess("Confirmed the signin, everyone should thank ${commandEvent.member.effectiveName}! You can now use voice commands.")
            metricsManager!!.markOauthComplete(true)
            client.close()
            return
        }
    }

    private fun poll(): OauthPollResponse {
        val requestJson: String = JSONObject()
                .put("client_id", oauthProperties.clientId)
                .put("client_secret", oauthProperties.clientSecret)
                .put("code", deviceCode)
                .put("grant_type", oauthProperties.oauthGrantType)
                .toString()

        val request = HttpPost(oauthProperties.oauthPollAddress)
        val body = StringEntity(requestJson, ContentType.APPLICATION_JSON)
        request.entity = body
        while (true) {
            try {
                client.execute(request).use { response ->
                    val responseBody = response.entity.content.readBytes().decodeToString()
                    return OauthPollResponse.fromJson(JSONObject(responseBody))
                }
            } catch (e: Exception) {
                log.error("Unknown error in oauth poller", e)
                return OauthPollResponse("","","",Instant.now(),e.message ?: "RIP")
            }
        }
    }
}

data class OauthPollResponse(val tokenType: String,
        val accessToken: String,
        val refreshToken: String,
        val tokenExpires: Instant,
        val errorMessage: String) {
    companion object {
        fun fromJson(json: JSONObject): OauthPollResponse {
            if (json.has("error") && !json.isNull("error")) {
                return OauthPollResponse("","","",Instant.now(), json.getString("error"))
            }
            val expiresIn: Long = json.getLong("expires_in")
            return OauthPollResponse(
                    json.getString("token_type"),
                    json.getString("access_token"),
                    json.optString("refresh_token"),
                    Instant.ofEpochSecond(System.currentTimeMillis() + expiresIn * 1000 - 60000),
                    ""
            )
        }
    }
}