package com.bot.models

data class RssUpdate(val channel: String, val url: String, val provider: Int, val subject: String, val displayName: String)