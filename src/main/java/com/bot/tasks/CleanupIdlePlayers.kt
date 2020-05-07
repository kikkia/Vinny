package com.bot.tasks

import com.bot.ShardingManager
import com.bot.utils.Logger
import com.bot.voice.VoiceSendHandler
import java.util.stream.Collectors

class CleanupIdlePlayers : Thread() {

    val logger = Logger(CleanupIdlePlayers::javaClass.name)

    override fun run() {
        val shards = ShardingManager.getInstance().shards.values.stream().map { v -> v.jda }.collect(Collectors.toList())
        logger.info("Starting cleanup for idle voice connections")
        var cleaned = 0
        for (shard in shards) {
            for (manager in shard.audioManagers) {
                if (!manager.isConnected && !manager.isAttemptingToConnect) {
                    manager.closeAudioConnection()
                    cleaned++
                    continue
                }

                val handler = manager.sendingHandler as VoiceSendHandler

                if (manager.isConnected && !handler.isPlaying) {
                    manager.closeAudioConnection()
                    cleaned++
                }
            }
        }

        logger.info("Cleaned $cleaned idle voice connections")
    }
}