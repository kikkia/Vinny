package com.bot.models

class InternalVoiceChannel(id: String,
                           guildId: String,
                           name: String,
                           var isVoiceEnabled: Boolean) : InternalChannel() {

    init {
        this.id = id
        this.guildId = guildId
        this.name = name
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as InternalVoiceChannel?
        return isVoiceEnabled == that!!.isVoiceEnabled &&
                id == that.id &&
                guildId == that.guildId &&
                name == that.name
    }

}
