package com.bot.models.enums

import com.bot.utils.ConstantEmojis
import net.dv8tion.jda.api.entities.emoji.Emoji

enum class RepeatMode(val ezName: String, val emoji: Emoji) {
    REPEAT_ONE("one", ConstantEmojis.repeatOneEmoji),
    REPEAT_ALL("all", ConstantEmojis.repeatAllEmoji),
    REPEAT_NONE("none", ConstantEmojis.repeatNoneEmoji)
}