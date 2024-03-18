package com.bot.voice

import com.bot.utils.VinnyConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AutoplayClient {
    object VideoRequester {
        private val baseUrl = VinnyConfig.instance().voiceConfig.autoplayProvider
        private val searchUrl = VinnyConfig.instance().voiceConfig.autoplaySearch
        private val httpClient = OkHttpClient()

        // Get a list of recommended video ids for a given video id.
        fun getRecommendedVideoIds(videoId: String): List<String> {
            val url = baseUrl + videoId
            val request = Request.Builder()
                .url(url)
                .build()

            return httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected response code: ${response.code}")
                }

                val json = response.body!!.string()
                val jsonObject = JSONObject(json)
                val recommendedVideosArray = jsonObject.getJSONArray("recommendedVideos")

                val videoUris = mutableListOf<String>()
                for (i in 0 until recommendedVideosArray.length()) {
                    val videoObject = recommendedVideosArray.getJSONObject(i)
                    videoUris.add(getAutoplayURI(videoObject.getString("videoId")))
                }

                videoUris
            }
        }

        // Gets the recommended videos for a title rather than a video ID.
        fun getRecommendedVideoIdsSearch(title: String) : List<String> {
            val url = searchUrl + title
            val request = Request.Builder()
                .url(url)
                .build()

            val videoId = httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected response code: ${response.code}")
                }

                val json = response.body!!.string()
                val jsonObject = JSONArray(json)

                val videoObject = jsonObject.getJSONObject(0)
                videoObject.getString("videoId")
            }
            return getRecommendedVideoIds(videoId)
        }

        private fun getAutoplayURI(id: String): String {
            return "${VinnyConfig.instance().voiceConfig.autoplayPrefix ?: ""}$id"
        }
    }
}