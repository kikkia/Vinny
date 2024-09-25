package com.bot.voice

import java.net.URI

object UrlTranslationUtils {
    val tubRegex = Regex("(?:(?:www\\.)?youtube\\.com\\/watch\\?v=|(youtu\\.be\\/)|youtube\\.com\\/shorts\\/)([A-Za-z0-9_-]{11})")
    val validDomains = listOf("twitch.tv", "soundcloud.com", "vimeo.com", "bandcamp.com")
    val redirectDomains = listOf("youtube.com", "youtu.be")

    fun translateUrl(url: String): AudioURLResult {
        val host = getHostFromUrl(url).replace("www.", "")
        if (!validDomains.contains(host)) {
            if (redirectDomains.contains(host)) {
                val groups = tubRegex.find(url)?.groupValues
                if (groups != null) {
                    if (groups.size == 3) {
                        return AudioURLResult(url, groups[2], true, true)
                    }
                }
            }
            return AudioURLResult(url, "", false, false)
        } else {
            return AudioURLResult(url, "", true, false)
        }

    }


    private fun getHostFromUrl(url: String): String {
        return try {
            val uri = URI.create(url)
            uri.host
        } catch (e: Exception) {
            ""
        }
    }
}

data class AudioURLResult(
        val url:String,
        val extractedId: String,
        val valid: Boolean,
        val useProvider: Boolean)
