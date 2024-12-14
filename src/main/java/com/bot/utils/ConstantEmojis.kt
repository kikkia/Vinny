package com.bot.utils

import net.dv8tion.jda.api.entities.emoji.Emoji

class ConstantEmojis {
    companion object {
        val playEmoji = Emoji.fromCustom("play", 1317540581932667050, false)
        val stopEmoji = Emoji.fromCustom("stop", 1317540872794931371, false)
        val pauseEmoji = Emoji.fromCustom("pause", 1317540700102856704, false)
        val nextEmoji = Emoji.fromCustom("next", 1317543736091217991, false)
        val shuffleEmoji = Emoji.fromCustom("shuffle", 1317540659590074399, false)
        val shuffleOffEmoji = Emoji.fromCustom("shuffleoff", 1317540736404819988, false)
        val repeatAllEmoji = Emoji.fromCustom("repeatall", 1317540493566939277, false)
        val repeatNoneEmoji = Emoji.fromCustom("repeatoff", 1317540363186995200, false)
        val repeatOneEmoji = Emoji.fromCustom("repeatone", 1317540433500442764, false)
    }
}