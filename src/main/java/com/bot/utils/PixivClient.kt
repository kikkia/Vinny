package com.bot.utils

import com.bot.caching.RedisCache
import com.bot.caching.RedisCache.Companion.getInstance
import com.bot.exceptions.NoSuchResourceException
import com.bot.exceptions.PixivException
import com.bot.models.PixivPost
import com.bot.utils.VinnyConfig.Companion.instance
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClients
import org.json.JSONException
import org.json.JSONObject
import java.util.*

// TODO: Caching mechanism
class PixivClient private constructor() {
    private val logger = Logger(PixivClient::class.java.name)
    private val proxyUrl = "pixiv.kikkia.dev"
    private val pixivReplaceUrl = "i.pximg.net"
    private val pixivLoginUrl = "https://pixiv.kikkia.dev/login/login"
    private val random = Random()
    private var redisCache: RedisCache? = null
    private val REDIS_KEY = "-pixiv-"
    private val SESSION_TTL = 28 * 86400L
    private var pixivSession = ""

    // Extremely basic locking mechanism to avoid spamming pixiv login server.
    private var loggingIn = false
    fun setSession(session: String) {
        pixivSession = session
        redisCache?.putString(REDIS_KEY, session, SESSION_TTL)
    }

    init {
        // If redis enabled try to pull session from redis
        val (_, _, _, _, _, _, _, cachingConfig) = instance()
        if (cachingConfig != null && java.lang.Boolean.TRUE == cachingConfig.pixivEnabled) {
            redisCache = getInstance()
            val cachedSess = redisCache?.getStr(REDIS_KEY)
            if (cachedSess != null) {
                pixivSession = cachedSess
            }
        }
    }

    @Throws(PixivException::class)
    fun getRandomPixivPostFromSearch(search: String, nsfw: Boolean): PixivPost {
        val baseUrl = "https://www.pixiv.net/ajax/search/artworks/"
        val nsfwTag = if (nsfw) "r18" else "safe"
        val page = random.nextInt(5).toString() // Get random page to get result from to make pool bigger
        try {
            HttpClients.createDefault().use { client ->
                if (pixivSession.isEmpty() && !loggingIn) {
                    login()
                }
                val get = HttpGet(baseUrl + search)
                val uri = URIBuilder(get.uri)
                    .addParameter("word", search)
                    .addParameter("order", "date_d")
                    .addParameter("mode", nsfwTag)
                    .addParameter("p", page)
                    .addParameter("s_mode", "s_tag")
                    .addParameter("type", "all")
                    .addParameter("lang", "en")
                    .build()
                get.addHeader("cookie", "PHPSESSID=$pixivSession")
                get.addHeader(
                    "user-agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0"
                )
                get.addHeader("authority", "www.pixiv.net")
                get.addHeader("referer", "https://www.pixiv.net/en/tags/megumin/artworks?mode=r18&s_mode=s_tag")
                get.addHeader("x-user-id", "22758490")
                get.addHeader("sec-ch-ua-platform", "Windows")
                get.addHeader("sec-fetch-site", "same-origin")
                get.addHeader("sec-fetch-mode", "cors")
                get.addHeader("accept", "application/json")
                get.addHeader("sec-fetch-dest", "empty")
                get.addHeader("accept-language", "en-US,en;q=0.9")
                get.addHeader("sec-ch-ua-mobile", "?0")
                get.uri = uri
                val response: HttpResponse = client.execute(get)
                return try {
                    val jsonResponse =
                        JSONObject(IOUtils.toString(response.entity.content))
                    if (jsonResponse.getBoolean("error")) {
                        logger.severe("Error getting pixiv post", PixivException(jsonResponse.toString()))
                        throw PixivException("Failed to get a pixiv post")
                    }
                    val data = jsonResponse.getJSONObject("body")
                    val illustManga = data.getJSONObject("illustManga")
                    val posts = illustManga.getJSONArray("data")
                    if (posts.length() == 0) {
                        throw NoSuchResourceException("No posts were found for that search")
                    }
                    val post = posts.getJSONObject(random.nextInt(posts.length()))
                    // Since discord doesnt embed pixiv images due to stuff on pixivs end
                    // we proxy the image url to allow embedding in discord.
                    PixivPost(
                        post.getString("id"), post.getString("title"),
                        getLink(post.getString("id")), post.getString("userName"), post.getString("userId"),
                        realPreviewURL(post.getString("url"))
                    )
                } catch (e: JSONException) {
                    logger.severe("Failed to parse pixiv response", e)
                    throw NoSuchResourceException("Could not find any results for tags")
                }
            }
        } catch (e: Exception) {
            logger.warning("Exception getting pixiv post", e)
            throw PixivException(e.message!!)
        }
    }

    private fun getLink(id: String): String {
        return "<https://www.pixiv.net/en/artworks/$id>"
    }

    private fun realPreviewURL(previewUrl: String): String {
        return previewUrl.replace(pixivReplaceUrl, proxyUrl)
            .replace("square", "master")
            .replace("c/250x250_80_a2/", "")
            .replace("custom_thumb", "img-master")
            .replace("custom-thumb", "img-master")
            .replace("_custom", "_master")
    }

    @Synchronized
    @Throws(PixivException::class)
    private fun login() {
        loggingIn = true
        val (_, _, _, _, thirdPartyConfig) = instance()
        val username = thirdPartyConfig!!.pixivUser
        val password = thirdPartyConfig.pixivPass
        if (username == null || password == null) {
            throw PixivException("Pixiv commands are not setup on this bot at the moment.")
        }
        try {
            HttpClients.createDefault().use { client ->
                val get = HttpGet(pixivLoginUrl)
                val uri = URIBuilder(get.uri)
                    .addParameter("username", username)
                    .addParameter("password", password)
                    .build()
                get.uri = uri
                val response: HttpResponse = client.execute(get)
                val jsonResponse = JSONObject(IOUtils.toString(response.entity.content))
                val cookies = jsonResponse.getJSONArray("cookies")
                // Iterate over the cookie list until we find the session id
                for (i in 0 until cookies.length()) {
                    val cookie = cookies.getJSONObject(i)
                    if (cookie.getString("name") == "PHPSESSID") {
                        pixivSession = cookie.getString("value")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        var instance: PixivClient? = null
            get() {
                if (field == null) field = PixivClient()
                return field
            }
            private set
    }
}