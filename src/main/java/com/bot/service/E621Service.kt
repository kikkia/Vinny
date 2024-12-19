package com.bot.service

import com.bot.caching.E621Cache
import com.bot.exceptions.newstyle.UserVisibleException
import com.bot.utils.Logger
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class E621Service {
    private val cache = E621Cache.getInstance()
    private val logger = Logger(this.javaClass.name)

    fun getPostsForSearch(search: String): List<String> {
        val formattedSearch = search.replace(" ".toRegex(), "%20")
        val baseUrl = "https://e621.net/posts.json?tags="
        val limit = "&limit=250"

        val images: List<String>? = cache.get(search)
        if (!images.isNullOrEmpty()) {
            return images
        }
        HttpClients.createDefault().use { client ->
            val get = HttpGet(baseUrl + formattedSearch + limit)
            val response: HttpResponse = client.execute(get)
            try {
                val jsonResponse = JSONObject(IOUtils.toString(response.entity.content))
                val posts = jsonResponse.getJSONArray("posts")
                if (posts.length() == 0) {
                    throw UserVisibleException("E621_NONE_FOUND")
                }
                val fetchedImages = ArrayList<String>()
                for (i in 0..<posts.length()) {
                    try {
                        fetchedImages.add(posts.getJSONObject(i).getJSONObject("file").getString("url"))
                    } catch (ignored: Exception) {
                        // Null url to image, we can generate our own with the md5 hash and file ext
                        try {
                            fetchedImages.add(buildE621StaticPath(posts.getJSONObject(i).getJSONObject("file")))
                        } catch (ignored2: Exception) {
                            // If that attempt fails, just skip
                        }
                    }
                }
                if (fetchedImages.isEmpty()) throw UserVisibleException("E621_NONE_FOUND")
                cache.put(search, fetchedImages)
                return fetchedImages
            } catch (e: JSONException) {
                logger.severe("Failed to parse e621 response", e)
                throw UserVisibleException("GENERIC_COMMAND_ERROR")
            }
        }
    }

    fun getAutocomplete(query: String): List<String> {
        val url = "https://e621.net/tags/autocomplete.json?search[name_matches]=$query&expiry=7"

        val client = HttpClients.createDefault()

        val request = HttpGet(url)

        client.execute(request).use { response ->
            if (response.statusLine.statusCode == 200) {
                val responseBody = EntityUtils.toString(response.entity)

                val jsonArray = JSONArray(responseBody)
                val tagNames = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val name = jsonObject.getString("name")
                    tagNames.add(name)
                }

                return tagNames
            } else {
                throw Exception("Request failed with status code: ${response.statusLine.statusCode}")
            }
        }
    }


    private fun buildE621StaticPath(jsonObject: JSONObject): String {
        val hash = jsonObject.getString("md5")
        return "https://static1.e621.net/data/" + hash.substring(0, 2) + "/" + hash.substring(2, 4) + "/" +
                hash + "." + jsonObject.getString("ext")
    }

    companion object {
        private var instance: E621Service? = null
        fun getInstance(): E621Service {
            if (instance == null) {
                instance = E621Service()
            }
            return instance!!
        }
    }
}