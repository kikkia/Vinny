package com.bot.models

enum class RssProvider(val value: Int) {
    REDDIT(1), TWITTER(2), CHAN(3), YOUTUBE(4), OTHER(0);

    companion object {
        fun getProvider(value: Int): RssProvider {
            return when (value) {
                1 -> REDDIT
                2 -> TWITTER
                3 -> CHAN
                4 -> YOUTUBE
                else -> OTHER
            }
        }
    }
}