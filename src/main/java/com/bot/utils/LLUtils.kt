package com.bot.utils

import com.bot.exceptions.OauthInjectException
import dev.arbjerg.lavalink.client.LavalinkNode
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class LLUtils {
    companion object {
        private val client = OkHttpClient()
        fun injectOauth(token: String, ident: String, node: LavalinkNode): Boolean {
            val json = """
        {
            "token": "$token",
            "identifier": "$ident"
        }
        """.trimIndent()

            val requestBody = json.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                    .url(node.baseUri + "/oauth")
                    .header("Authorization", node.password)
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                return true
            } else {
                throw OauthInjectException("Oauth injection failed with ${response.code}")
            }
        }
    }
}