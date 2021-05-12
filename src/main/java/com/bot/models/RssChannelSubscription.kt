package com.bot.models

import com.bot.ShardingManager

data class RssChannelSubscription(val id: Int, val rssSubscription: RssSubscription, val channel: String, val author: String) {
    override fun toString(): String {
        val channelName = ShardingManager.getInstance().getChannel(channel)?.name ?: "Channel not found :thonk:"
        val authorName = ShardingManager.getInstance().getUserFromAnyShard(author)?.name ?: "Author not found :thonk:"
        val subject = rssSubscription.displayName ?: rssSubscription.subject
        return "ID: $id\nProvider: ${rssSubscription.provider.name}\nSubject: $subject\nChannel: $channelName (${channel})\nAuthor: $authorName"
    }

    fun toCondensedString(): String {
        val subject = rssSubscription.displayName ?: rssSubscription.subject
        val authorName = ShardingManager.getInstance().getUserFromAnyShard(author)?.name ?: "Author not found :thonk:"
        return "ID: $id Type: ${rssSubscription.provider.name} Subject: $subject Author: $authorName"
    }
}