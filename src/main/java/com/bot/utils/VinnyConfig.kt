package com.bot.utils

import com.sksamuel.hoplite.Secret

data class VinnyConfig(val discordConfig: DiscordConfig,
                       val databaseConfig: DatabaseConfig,
                       val shardingConfig: ShardingConfig,
                       val botConfig: BotConfig,
                       val thirdPartyConfig: ThirdPartyConfig,
                       val rssConfig: RSSConfig,
                       val voiceConfig: VoiceConfig) {
}

data class BotConfig(val enableScheduledCommands: Boolean,
                     val enableLoggingChannels: Boolean,
                     val hostIdentifier: String,
                     val guildPrefsCacheTTL: Int,
                     val onlineEmoji: String,
                     val idleEmoji: String,
                     val dndEmoji: String,
                     val offlineEmoji: String,
                     val debugWebhooks: List<String>,
                     val infoWebhooks: List<String>,
                     val warningWebhooks: List<String>,
                     val errorWebhooks: List<String>)

data class DiscordConfig(val discordToken: String,
                         val botId: String,
                         val ownerId: String)

data class DatabaseConfig(val databaseAddress: String,
                          val databaseUsername: String,
                          val databasePassword: String,
                          val schema: String)

data class ShardingConfig(val totalShards: Int,
                          val localShardStart: Int,
                          val localShardEnd: Int)

data class VoiceConfig(val nodes: List<LavalinkNode>)

data class LavalinkNode(val address: String,
                        val password: String,
                        val region: String)

data class RSSConfig(val enableNATS: Boolean,
                     val natsAddress: String,
                     val natsPassword: String)

data class ThirdPartyConfig(val p90Token: String,
                            val twitchClientId: String,
                            val pixivUser: String,
                            val pixivPass: String,
                            val sauceProxy: String,
                            val sauceToken: String,
                            val datadogHostname: String,
                            val redditClientId: String,
                            val redditClientToken: String)