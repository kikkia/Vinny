package com.bot.utils

import org.json.JSONArray
import org.json.JSONObject
import java.util.*

fun getSFWSubmission(results: JSONArray): JSONObject? {
    var chosenSubmission: JSONObject? = null
    val random = Random()

    for (i in 1..1000) {
        val chosenIndex = random.nextInt(results.length())
        val result = results.getJSONObject(chosenIndex)
        if ("all-age" == result.getString("age_limit") &&
                !getJSONStringArrayAsList(result.getJSONArray("tags")).contains("R-18")
                && result.getString("sanity_level")=="white") {
            chosenSubmission = result
            break
        }
    }

    return chosenSubmission
}

fun buildPreviewString(previewUrl: String) : String {
    var url = previewUrl.replace(".jpg", "_master1200.jpg")
    url =  url.replace(".png", "_master1200.jpg")
    url =  url.replace(".gif", "_master1200.jpg")
    return url.replace("img-original", "img-master")
}

fun getJSONStringArrayAsList(json: JSONArray) : List<String?> {
    val arr = arrayOfNulls<String>(json.length())
    for (i in 0 until json.length()) arr[i] = json.getString(i)
    return arr.toList()
}