package com.bot.utils


import com.bot.tasks.OauthCheckerTask
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.util.*


data class OauthSetupResponse(val userCode: String,
                              val deviceCode: String,
                              val interval: Long,
                              val expiresIn: Long,
                              val verificationUrl: String,
                              val successful: Boolean) {
    companion object {
        fun fromJsonObject(json: JSONObject): OauthSetupResponse {
            return OauthSetupResponse(
                json.getString("user_code"),
                json.getString("device_code"),
                json.getLong("interval"),
                json.getLong("expires_in"),
                json.getString("verification_url"),
                true
            )
        }
    }
}

class Oauth2Utils {
    private var enabled = false
    private var refreshToken: String = ""
    private var tokenType: String = ""
    private var accessToken: String = ""
    private var tokenExpires: Long = 0

    fun shouldRefreshAccessToken(): Boolean {
        return enabled && refreshToken.isNotEmpty() && (accessToken.isEmpty() || System.currentTimeMillis() >= tokenExpires)
    }

     fun initializeAccessToken(commandEvent: CommandEvent): OauthSetupResponse {
        val response: OauthSetupResponse = fetchDeviceCode()
        // Lets get a poller going to check for the flow completion
        OauthCheckerTask(response.deviceCode, if (response.interval == 0L) 5000 else response.interval, commandEvent).start()
        return response
     }

    private fun fetchDeviceCode(): OauthSetupResponse {
        val httpClient = HttpClients.createDefault()

        val requestJson: String = JSONObject()
                .put("client_id", CLIENT_ID)
                .put("scope", SCOPES)
                .put("device_id", UUID.randomUUID().toString().replace("-", ""))
                .put("device_model", "ytlr::")
                .toString()
        val request = HttpPost("https://www.youtube.com/o/oauth2/device/code")
        val body = StringEntity(requestJson, ContentType.APPLICATION_JSON)
        request.entity = body
        httpClient.use { client ->
            client.execute(request).use { response ->
                val statusCode = response.statusLine.statusCode
                if (statusCode == 200) {
                    val responseJson = JSONObject(response.entity.content.readBytes())
                    // Process the response JSON object
                    println("Response JSON: $responseJson")
                    return OauthSetupResponse.fromJsonObject(responseJson)
                } else {
                    println("Error: ${response.statusLine}")
                }
            }
        }
        return OauthSetupResponse("", "", 0, 0, "", false)
    }

    companion object {
        internal const val CLIENT_ID = "861556708454-d6dlm3lh05idd8npek18k6be8ba3oc68.apps.googleusercontent.com"
        internal const val CLIENT_SECRET = "SboVhoG9s0rNafixCSGGKXAT"
        private const val SCOPES = "http://gdata.youtube.com https://www.googleapis.com/auth/youtube"
    }
}