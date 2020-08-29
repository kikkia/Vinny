package com.bot.utils

import club.minnced.discord.webhook.send.WebhookMessage
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.bot.db.RssDAO
import com.bot.models.RssProvider
import com.bot.models.RssUpdate
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import org.json.JSONObject

class RssUtils {
    companion object {
        val logger = Logger(this::class.java.simpleName)

        fun sendRssUpdate(rssUpdate: RssUpdate, jda : JDA) {
            // TODO: Cache RSS-Subscriptions to avoid extra db ops
            val channel = jda.getTextChannelById(rssUpdate.channel)
            if (channel == null) {
                logger.warning("Failed to find text channel for RSS update $channel")
                return
            }

            // Like a dingus sometimes the subject has some shitty prefix FIX THIS BALMERS CURVE SHIT
            val cleanedSubject = rssUpdate.subject
                    .replace("**VINNY**RT", "")
                    .replace("**VINNY**Live", "")

            if (!channel.isNSFW &&
                    RssDAO.getInstance().getBySubjectAndProvider(rssUpdate.subject, rssUpdate.provider).nsfw) {
                channel.sendMessage(ConstantStrings.RSS_NSFW_DENY).queue()
                return
            }

            if (!channel.guild.selfMember.hasPermission(Permission.MANAGE_WEBHOOKS)) {
                channel.sendMessage("WARNING: I don't have the `MANAGE_WEBHOOKS` permission. Please give me this permission " +
                        "to allow Scheduled commands and Subscriptions to work correctly").queue()
            } else {
                val webhook = ScheduledCommandUtils.getWebhookForChannel(channel)
                when (RssProvider.getProvider(rssUpdate.provider)) {
                    RssProvider.REDDIT -> {
                        webhook.send(buildMessage("New post in ***${rssUpdate.displayName}***" +
                                "\nhttps://reddit.com${rssUpdate.url}", jda))
                    }
                    RssProvider.TWITTER -> {
                        val msg = if (rssUpdate.subject.startsWith("**VINNY**RT")) {
                            "New retweet from ***${rssUpdate.displayName}"
                        } else {
                            "New tweet from ***${rssUpdate.displayName}"
                        }
                        webhook.send(buildMessage("$msg***\n" + rssUpdate.url, jda))
                    }
                    RssProvider.CHAN -> {
                        webhook.send(buildMessage("New thread in ***${rssUpdate.displayName}***\n" +
                                rssUpdate.url, jda))
                    }
                    RssProvider.YOUTUBE -> {
                        val msg = if (rssUpdate.subject.startsWith("**VINNY**Live")) {
                            "***${rssUpdate.displayName}*** has started streaming!\n" +
                                    rssUpdate.url
                        } else {
                            "New video posted from ***${rssUpdate.subject}***\n" +
                                    rssUpdate.url
                        }
                        webhook.send(buildMessage(msg, jda))
                    }
                    RssProvider.TWITCH -> {
                        val msg = "***${rssUpdate.displayName}*** just went live on twitch! ${rssUpdate.url}"
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
                    json.getString("subject"),
                    json.getString("displayName")
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