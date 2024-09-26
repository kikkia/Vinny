package com.bot.tasks

import com.bot.utils.Oauth2Utils
import com.bot.voice.GuildOauthConfig
import com.bot.voice.GuildVoiceProvider
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.json.JSONObject
import org.slf4j.LoggerFactory

class OauthCheckerTask(val deviceCode: String, 
                       val interval: Long, 
                       val commandEvent: CommandEvent): Thread() {
    val client = HttpClients.createDefault()
    val guildProvider = GuildVoiceProvider.getInstance()
    private val log = LoggerFactory.getLogger(OauthCheckerTask::class.java)


    override fun run() {
        while (true) {
            val response = poll()
            if (response.errorMessage.isNotEmpty()) {
                when(response.errorMessage) {
                    "authorization_pending", "slow_down" -> {
                        sleep(interval)
                        continue
                    }
                    // TODO: Metrics
                    "expired_token" -> {
                        commandEvent.replyError("Linking to account failed. It looks like it's timed out. Please try again.")
                    }

                    "access_denied" -> {
                        commandEvent.replyError("Linking to account was denied. :( Please try again.")
                    }

                    else -> {
                        commandEvent.replyError("An unknown error occurred while checking for signin completion.")
                        log.error("Error with Oauth2 flow: {}", response.errorMessage)
                    }
                }
                client.close()
                return
            }
            commandEvent.replySuccess("Successfully retrieved token ${response.refreshToken} --- ${response.accessToken}")
            val conn = guildProvider.getGuildVoiceConnection(commandEvent.guild)
            conn.oauthConfig = GuildOauthConfig(response.tokenType, response.accessToken, response.refreshToken, response.tokenExpires)
            client.close()
            return
        }
    }

    fun poll(): OauthPollResponse {
        val requestJson: String = JSONObject()
                .put("client_id", Oauth2Utils.CLIENT_ID)
                .put("client_secret", Oauth2Utils.CLIENT_SECRET)
                .put("code", deviceCode)
                .put("grant_type", "http://oauth.net/grant_type/device/1.0")
                .toString()

        val request = HttpPost("https://www.youtube.com/o/oauth2/token")
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
                return OauthPollResponse("","","",0,e.message ?: "RIP")
            }
        }
    }
}

data class OauthPollResponse(val tokenType: String,
        val accessToken: String,
        val refreshToken: String,
        val tokenExpires: Long,
        val errorMessage: String) {
    companion object {
        fun fromJson(json: JSONObject): OauthPollResponse {
            if (json.has("error") && !json.isNull("error")) {
                return OauthPollResponse("","","",0, json.getString("error"))
            }
            val expiresIn: Long = json.getLong("expires_in")
            return OauthPollResponse(
                    json.getString("token_type"),
                    json.getString("access_token"),
                    json.optString("refresh_token"),
                    System.currentTimeMillis() + expiresIn * 1000 - 60000,
                    ""
            )
        }
    }
}