package com.bot.utils

import com.bot.exceptions.NoSuchResourceException
import com.bot.utils.VinnyConfig.Companion.instance
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.json.JSONObject
import java.io.IOException

class TwitchUtils {
    companion object {
        val config: VinnyConfig = instance()

        private var oauthToken: String? = null
        private var tokenExpiration: Long? = null

        @Throws(IOException::class, NoSuchResourceException::class)
        fun getTwitchIdForUsername(username: String): String {
            if (needsRefresh()) {
                refreshToken()
            }
            val uri = "https://api.twitch.tv/helix/users?login=$username"
            HttpClients.createDefault().use { client ->
                val httpget = HttpGet(uri)
                httpget.addHeader("Client-ID", config.thirdPartyConfig!!.twitchClientId)
                httpget.addHeader("Authorization", "Bearer $oauthToken")
                httpget.addHeader("Accept", "application/vnd.twitchtv.v5+json")
                val response: HttpResponse = client.execute(httpget)
                val jsonResponse = JSONObject(IOUtils.toString(response.entity.content))
                if (jsonResponse.getJSONArray("data").length() == 0) {
                    throw NoSuchResourceException("User not found")
                }
                return jsonResponse.getJSONArray("data").getJSONObject(0).getString("id")
            }
        }

        private fun needsRefresh() : Boolean {
            return oauthToken == null || System.currentTimeMillis() >= tokenExpiration!!
        }

        private fun refreshToken() {
            val url =
                "https://id.twitch.tv/oauth2/token?client_id=${config.thirdPartyConfig!!.twitchClientId}&client_secret=${config.thirdPartyConfig.twitchClientSecret}&grant_type=client_credentials"
            HttpClients.createDefault().use { client ->
                val httpPost = HttpPost(url)
                val response: HttpResponse = client.execute(httpPost)
                val jsonResponse = JSONObject(IOUtils.toString(response.entity.content))
                oauthToken = jsonResponse.getString("access_token")
                tokenExpiration = System.currentTimeMillis() + (jsonResponse.getInt("expires_in") - 1) * 1000L
            }

        }
    }
}