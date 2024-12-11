package com.bot.models

import com.bot.ShardingManager
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

data class RssChannelSubscription(val id: Int, val rssSubscription: RssSubscription, val channel: String, val author: String) {
    override fun toString(): String {
        val channelName = ShardingManager.getInstance().getChannel(channel)?.name ?: "Channel not found :thonk:"
        val authorName = ShardingManager.getInstance().getUserFromAnyShard(author)?.name ?: "Author not found :thonk:"
        val subject = rssSubscription.displayName ?: rssSubscription.subject
        return "ID: $id\nProvider: ${rssSubscription.provider.name}\nSubject: $subject\nChannel: $channelName (${channel})\nAuthor: $authorName"
    }

    fun toEmbed(): MessageEmbed {
        val channelName = ShardingManager.getInstance().getChannel(channel)?.name ?: "Channel not found :thonk:"
        val authorName = ShardingManager.getInstance().getUserFromAnyShard(author)?.name ?: "Author not found :thonk:"
        val subject = rssSubscription.displayName ?: rssSubscription.subject
        val embedBuilder = EmbedBuilder()
            .setTitle("Subscription ID: $id")
            .setDescription("Provider: ${rssSubscription.provider.name}")
            .addField("Subject", subject, false)
            .addField("Author", authorName, false)
            .addField("Channel", channelName, false)
        return embedBuilder.build()
    }

    fun toCondensedString(): String {
        val subject = rssSubscription.displayName ?: rssSubscription.subject
        val authorName = ShardingManager.getInstance().getUserFromAnyShard(author)?.name ?: "Author not found :thonk:"
        return "ID: $id Type: ${rssSubscription.provider.name} Subject: $subject Author: $authorName"
    }
}