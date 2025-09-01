package com.bot.utils

import com.bot.caching.R34Cache
import com.bot.exceptions.newstyle.UserVisibleException
import com.bot.metrics.MetricsManager
import com.bot.models.enums.R34Provider
import com.bot.utils.TheGreatCCPFilter.Companion.containsNoNoTags
import org.apache.http.HttpResponse
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.ResponseHandler
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import java.util.regex.Pattern
import java.util.stream.Collectors

class R34Utils {

    companion object {
        val random = java.util.Random(System.currentTimeMillis())
        val cache = R34Cache.getInstance()
        val metricsManager = MetricsManager.instance
        val logger = Logger(this.javaClass.getSimpleName())
        val xxApiKey = VinnyConfig.instance().thirdPartyConfig?.r34ApiKey
        val xxUserId = VinnyConfig.instance().thirdPartyConfig?.r34UserId


        fun getPostForSearch(query: String) : String {
            var imageUrls: MutableList<String>? = cache.get(query)
            try {
                if (imageUrls == null) {
                    imageUrls = ArrayList()
                    imageUrls.addAll(getImageURLFromSearch(getXXUrl(query), R34Provider.XXX))
                    imageUrls.addAll(getImageURLFromSearch(getBooruUrl(query), R34Provider.YANDERE))
                    imageUrls.addAll(getImageURLFromSearch(getPahealUrl(query), R34Provider.PAHEAL))
                    cache.put(query, imageUrls)
                }
                if (imageUrls.isEmpty()) {
                    throw UserVisibleException("NO_RESULTS_FOUND")
                }
                return imageUrls[random.nextInt(imageUrls.size)]
            } catch (e: UserVisibleException) {
                throw e
            } catch (e: Exception) {
                if (imageUrls!!.isEmpty()) {
                    logger.severe("Something went wrong getting r34 post: ", e)
                    throw RuntimeException("Failed to fetch any r34")
                } else {
                    logger.warning("Failed to get some r34 posts, but some exist... Attempting to send them", e)
                    cache.put(query, imageUrls)
                    return imageUrls[random.nextInt(imageUrls.size)]
                }
            }
        }

        private fun getImageURLFromSearch(url: String, provider: R34Provider): List<String> {
            val get = HttpGet(url)
            metricsManager!!.markR34Request(provider)
            val timeout = 4
            val config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build()

            try {
                HttpClientBuilder.create().setDefaultRequestConfig(config).build().use { client ->
                    val responseHandler = ResponseHandler { response: HttpResponse ->
                        val status = response.statusLine.statusCode
                        if (status in 200..299) {
                            val entity = response.entity
                            return@ResponseHandler if (entity != null) EntityUtils.toString(entity) else null
                        } else {
                            throw ClientProtocolException("Unexpected response status: $status")
                        }
                    }
                    val responseBody = client.execute(get, responseHandler)
                    client.close()

                    // Regex the returned xml and get all links different regex based on source
                    val expression = if (url.contains("paheal.net"))
                        Pattern.compile("(<media:content url=)\"([\\s\\S]*?)\"\\/>")
                    else
                        Pattern.compile("(sample_url)=[\"']?((?:.(?![\"']?\\s+(?:\\S+)=|[>\"']))+.)[\"']?")
                    val matcher = expression.matcher(responseBody)
                    var possibleLinks: MutableList<String> = java.util.ArrayList()

                    while (matcher.find()) {
                        // Add the second group of regex
                        possibleLinks.add(matcher.group(2))
                    }

                    // Some URLs contain post tags, scan URLs for things banned on discord
                    possibleLinks = possibleLinks.stream().filter { it: String? ->
                        !containsNoNoTags(
                            it!!
                        )
                    }
                        .collect(Collectors.toList())
                    metricsManager.markR34Response(provider, true)
                    metricsManager.markR34ResponseSize(provider, possibleLinks.size.toLong())
                    return possibleLinks
                }
            } catch (e: java.lang.Exception) {
                metricsManager.markR34Response(provider, false)
                logger.warning("Failed to fetch r34 posts for source: $url", e)
                return java.util.ArrayList()
            }
        }

        fun getAutocomplete(input: String): List<Pair<String, String>> {
            val client = HttpClients.createDefault()
            val url = "https://ac.rule34.xxx/autocomplete.php?q=$input"

            val request = HttpGet(url)
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Firefox/91.0")

            client.execute(request).use { response ->
                if (response.statusLine.statusCode == 200) {
                    val responseBody = EntityUtils.toString(response.entity)
                    val jsonArray = JSONArray(responseBody)

                    return (0 until jsonArray.length()).map { index ->
                        val jsonObject = jsonArray.getJSONObject(index)
                        val label = jsonObject.getString("label")
                        val value = jsonObject.getString("value")
                        label to value
                    }
                } else {
                    throw Exception("Request failed: ${response.statusLine}")
                }
            }
        }

        private fun getXXUrl(query: String): String {
            return "http://api.rule34.xxx/index.php?page=dapi&s=post&q=index&limit=200&tags=$query&api_key=${xxApiKey}&user_id=${xxUserId}"
        }

        private fun getBooruUrl(query: String): String {
            return "https://yande.re/post.xml?limit=200&tags=$query"
        }

        private fun getPahealUrl(query: String): String {
            return "https://rule34.paheal.net/rss/images/" + query.replace(" ".toRegex(), "%20") + "/1"
        }
    }
}