package com.bot.models

class InternalGuildMembership(var id: String?, var guildId: String?, private var canUseBot: Boolean, private var premium: Boolean) {
    var name: String? = null

    fun canUseBot(): Boolean {
        return canUseBot
    }

    fun setCanUseBot(canUseBot: Boolean) {
        this.canUseBot = canUseBot
    }
}
