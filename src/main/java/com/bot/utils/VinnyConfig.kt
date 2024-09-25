package com.bot.utils

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import jdk.jfr.Enabled


data class VinnyConfig(val discordConfig: DiscordConfig,
                       val databaseConfig: DatabaseConfig,
                       val shardingConfig: ShardingConfig,
                       val botConfig: BotConfig,
                       val thirdPartyConfig: ThirdPartyConfig?,
                       val rssConfig: RSSConfig,
                       val voiceConfig: VoiceConfig,
                       val cachingConfig: CachingConfig?) {

    companion object {
        private var inst: VinnyConfig? = null

        fun instance() : VinnyConfig {
            if (inst == null) {
                inst = ConfigLoaderBuilder.default()
                    .addResourceSource("/config.yaml")
                    .build()
                    .loadConfigOrThrow()
            }
            return inst!!
        }
    }
}

data class BotConfig(val enableScheduledCommands: Boolean,
                     val enableLoggingChannels: Boolean,
                     val hostIdentifier: String,
                     val guildPrefsCacheTTL: Int?,
                     val dataLoader: Boolean,
                     val silentDeploy: Boolean,
                     val onlineEmoji: String,
                     val idleEmoji: String,
                     val dndEmoji: String,
                     val offlineEmoji: String,
                     val debugWebhooks: List<String>?,
                     val infoWebhooks: List<String>?,
                     val warningWebhooks: List<String>?,
                     val errorWebhooks: List<String>?)

data class DiscordConfig(val token: String,
                         val botId: String,
                         val ownerId: String)

data class DatabaseConfig(val address: String,
                          val username: String,
                          val password: String,
                          val schema: String)

data class ShardingConfig(val total: Int,
                          val localStart: Int,
                          val localEnd: Int)

data class VoiceConfig(val nodes: List<LavalinkNode>?,
                       val defaultSearchProvider: String = "disabled",
                       val autoplayProvider: String?,
                       val autoplayToken: String?,
                       val autoplayPrefix: String?,
                       val autoplaySource: String?,
                       val autoplaySearch: String?,
                       val voiceProviderAPI: String?,
                       val voiceProviderFormat: String?)

data class LavalinkNode(val address: String,
                        val password: String,
                        val region: String?,
                        val name: String)

data class RSSConfig(val enable: Boolean,
                     val natsAddress: String?,
                     val natsPassword: String?,
                     val natsSubject: String?)

data class ThirdPartyConfig(val p90Token: String?,
                            val twitchClientId: String?,
                            val pixivUser: String?,
                            val pixivPass: String?,
                            val sauceProxy: String?,
                            val sauceToken: String?,
                            val datadogHostname: String?,
                            val redditClientId: String?,
                            val redditClientToken: String?)

data class CachingConfig(val enabled: Boolean,
    val pixivEnabled: Boolean?,
    val r34enabled: Boolean?,
    val redisUrl: String?,
    val redisUser: String?,
    val redisPassword: String?,
    val redisPort: String?)