package com.bot.utils

import com.bot.exceptions.InvalidInputException

class VoiceUtils {
    companion object {
        fun parseSeekPos(commandArgs: String) : Long {
            var input = commandArgs
            var neg = false
            if (commandArgs.startsWith("-")) {
                neg = true
                input = input.replace("-", "")
            } else if (commandArgs.isBlank()) {
                throw InvalidInputException("You need to provide somewhere to seek to. (e.g. 2:34:20 -> 2 hours, 34 mins, 20 secs)")
            }
            val parts = input.split(":")
            if (parts.size > 3) {
                throw InvalidInputException("Input has more than 3 parts. Input can only have Hours/Mins/Sec like (2:34:20 -> 2 hours, 34 mins, 20 secs)")
            }
            var totalMilliseconds = 0L
            for ((pos, i) in parts.indices.reversed().withIndex()) {
                val value = parts[i].toInt()
                val multiplier = when (pos) {
                    2 -> 3600000L // Hours
                    1 -> 60000L   // Minutes
                    0 -> 1000L    // Seconds
                    else -> throw IllegalArgumentException("Invalid time string format")
                }
                totalMilliseconds += value * multiplier
            }
            if (neg) {
                totalMilliseconds *= -1
            }
            return totalMilliseconds
        }
    }
}