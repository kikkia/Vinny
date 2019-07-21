package com.bot.utils

import org.json.JSONArray
import org.json.JSONObject
import java.util.*

fun getSFWSubmission(results: JSONArray): JSONObject? {
    var chosenSubmission: JSONObject? = null
    val random = Random()

    for (i in 1..20) {
        val chosenIndex = random.nextInt(results.length())
        if ("all-age" == results.getJSONObject(chosenIndex).getString("age_limit"))
            chosenSubmission = results.getJSONObject(chosenIndex)
            break
    }

    return chosenSubmission
}