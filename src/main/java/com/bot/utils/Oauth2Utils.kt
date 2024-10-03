package com.bot.utils


import com.bot.db.models.OauthConfig
import com.bot.exceptions.OauthRefreshException
import com.bot.metrics.MetricsManager
import com.bot.tasks.OauthCheckerTask
import com.bot.tasks.OauthPollResponse
import com.jagrosh.jdautilities.command.CommandEvent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
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
    companion object {
        val client = OkHttpClient()
        val oauthProperties = VinnyConfig.instance().voiceConfig.oauthConfig
        val metricsManager = MetricsManager.instance

         fun initOauthFlow(commandEvent: CommandEvent): OauthSetupResponse {
            val response: OauthSetupResponse = fetchDeviceCode()
            // Lets get a poller going to check for the flow completion
            OauthCheckerTask(response.deviceCode, if (response.interval == 0L) 10000 else response.interval, commandEvent).start()
            return response
         }

        private fun fetchDeviceCode(): OauthSetupResponse {
            val jsonObject = JSONObject()
                    .put("client_id", oauthProperties.clientId)
                    .put("scope", oauthProperties.scopes)
                    .put("device_id", UUID.randomUUID().toString().replace("-", ""))
                    .put("device_model", oauthProperties.device)

            val requestBody = RequestBody.create("application/json".toMediaType(), jsonObject.toString().toByteArray())

            val request = Request.Builder()
                    .url(oauthProperties.codeAddress)
                    .post(requestBody)
                    .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val statusCode = response.code
                    println("Error: Status code $statusCode")
                    return OauthSetupResponse("", "", 0, 0, "", false)
                }

                val responseBody = response.body!!.string()
                val responseJson = JSONObject(responseBody)
                // Process the response JSON object
                println("Response JSON: $responseJson")
                return OauthSetupResponse.fromJsonObject(responseJson)

            } catch (e: Exception) {
                println("Error: ${e.message}")
                return OauthSetupResponse("", "", 0, 0, "", false)
            }
        }

        fun refreshAccessToken(oauthConfig: OauthConfig): OauthConfig {
            println("Refreshing token")
            val jsonObject = JSONObject()
                    .put("client_id", oauthProperties.clientId)
                    .put("client_secret", oauthProperties.clientSecret)
                    .put("refresh_token", oauthConfig.refreshToken)
                    .put("grant_type", "refresh_token")
            val requestBody = RequestBody.create("application/json".toMediaType(), jsonObject.toString().toByteArray())
            val request = Request.Builder()
                    .url(oauthProperties.refreshAddress)
                    .post(requestBody)
                    .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val statusCode = response.code
                    println("Error: Status code $statusCode")
                    throw OauthRefreshException("Failed to refresh oauth token. Status code: $statusCode")
                }

                val responseBody = response.body!!.string()
                val responseJson = JSONObject(responseBody)
                // Process the response JSON object
                println("Response JSON: $responseJson")
                val parsed = OauthPollResponse.fromJson(responseJson)
                return OauthConfig(oauthConfig.userId, parsed.refreshToken, parsed.accessToken, parsed.tokenType, parsed.tokenExpires)

            } catch (e: Exception) {
                println("Error: ${e.message}")
                throw OauthRefreshException("Exception encountered during token refresh: ${e.message}")
            }
        }
    }
}