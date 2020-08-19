package com.bot.utils

class ChanUtils() {
    companion object {
        val SFW_BOARDS = listOf<String>(
                "g", "3", "a", "adv", "an", "asp", "biz", "c", "cgl", "ck", "cm", "co", "diy", "fa", "fit", "gd",
                "his", "int", "jp", "k", "lgbt", "lit", "m", "mlp", "mu", "n", "news", "o", "out", "p", "po", "qa",
                "qst", "sci", "sp", "tg", "toy", "trv", "tv", "v", "vg", "vip", "vp", "vr", "wsg", "x")
        val NSFW_BOARDS = listOf<String>(
                "aco", "b", "bant", "d", "e", "gif", "h", "hc", "hm", "hr", "i", "ic", "pol", "r", "r9k", "s",
                "s4s", "soc", "t", "trash", "u", "wg", "y", "w"
        )

        fun getBoard(name: String) : Board? {
            return when {
                SFW_BOARDS.contains(name) -> Board(name, false)
                NSFW_BOARDS.contains(name) -> Board(name, true)
                else -> null
            }
        }
    }

    data class Board(val name: String, val nsfw: Boolean)
}