package com.bot.utils

class TheGreatCCPFilter {
    companion object {
        // Words related to r34 banned on Discord
        val filteredTags = arrayOf("loli", "shota", "kid", "child", "young", "imouto",
                "cub", "jailbait", "cunny")
        fun containsNoNoTags(message: String) : Boolean {
            for (tag in filteredTags) {
                if (message.contains(tag)) {
                    return true
                }
            }
            return false
        }
    }
}