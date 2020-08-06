package com.bot.utils

import club.minnced.discord.webhook.send.WebhookMessage
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.bot.models.RssProvider
import com.bot.models.RssUpdate
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import org.json.JSONObject

class RssUtils {
    companion object {
        val logger = Logger(this::class.java.simpleName)

        fun sendRssUpdate(rssUpdate: RssUpdate, jda : JDA) {
            val channel = jda.getTextChannelById(rssUpdate.channel)
            if (channel == null) {
                logger.warning("Failed to find text channel for RSS update $channel")
                return
            }
            if (!channel.guild.selfMember.hasPermission(Permission.MANAGE_WEBHOOKS)) {
                channel.sendMessage("WARNING: I don't have the `MANAGE_WEBHOOKS` permission. Please give me this permission " +
                        "to allow Scheduled commands and Subscriptions to work correctly")
            } else {
                val webhook = ScheduledCommandUtils.getWebhookForChannel(channel)
                when (RssProvider.getProvider(rssUpdate.provider)) {
                    RssProvider.REDDIT -> {
                        webhook.send(buildMessage("New post in ***${rssUpdate.subject}***" +
                                "\nhttps://reddit.com${rssUpdate.url}", jda))
                    }
                    RssProvider.TWITTER -> {
                        val msg = if (rssUpdate.subject.startsWith("**VINNY**RT")) {
                            "New retweet from ***${rssUpdate.subject.replace("**VINNY**RT", "")}"
                        } else {
                            "New tweet from ***${rssUpdate.subject}"
                        }
                        webhook.send(buildMessage("$msg***\n" + rssUpdate.url, jda))
                    }
                    RssProvider.CHAN -> {
                        webhook.send(buildMessage("New thread in ***${rssUpdate.subject}***\n" +
                                rssUpdate.url, jda))
                    }
                    RssProvider.YOUTUBE -> {
                        val msg = if (rssUpdate.subject.startsWith("**VINNY**Live")) {
                            "***${rssUpdate.subject.replace("**VINNY**Live","")}*** has started streaming!\n" +
                                    rssUpdate.url
                        } else {
                            "New video posted from ***${rssUpdate.subject}***\n" +
                                    rssUpdate.url
                        }
                        webhook.send(buildMessage(msg, jda))
                    }
                    else -> { // other
                        logger.warning("Invalid provider for rss event: ```$rssUpdate```")
                    }
                }
            }
        }

        fun mapJsonToUpdate(json: JSONObject) : RssUpdate {
            return RssUpdate(
                    json.getString("channel"),
                    json.getString("url"),
                    json.getInt("provider"),
                    json.getString("subject")
            )
        }

        fun buildMessage(msg: String, jda: JDA) :WebhookMessage {
            return WebhookMessageBuilder()
                    .setUsername("Vinny")
                    .setAvatarUrl(jda.selfUser.avatarUrl)
                    .setContent(msg)
                    .build()
        }
    }
}