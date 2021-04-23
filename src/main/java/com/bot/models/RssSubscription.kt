package com.bot.models

data class RssSubscription(val id: Int,
                           val subject: String,
                           val provider: RssProvider,
                           val nsfw: Boolean,
                           val displayName: String?)