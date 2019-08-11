package com.bot.models


class InternalTextChannel(id: String,
                          guildId: String,
                          name: String,
                          var isAnnouncmentChannel: Boolean,
                          var isNSFWEnabled: Boolean,
                          var isCommandsEnabled: Boolean,
                          var isVoiceEnabled: Boolean) : InternalChannel() {

    var aliases: Map<String, Alias>? = null

    init {
        this.id = id
        this.guildId = guildId
        this.name = name
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as InternalTextChannel?
        return isAnnouncmentChannel == that!!.isAnnouncmentChannel &&
                isNSFWEnabled == that.isNSFWEnabled &&
                isCommandsEnabled == that.isCommandsEnabled &&
                isVoiceEnabled == that.isVoiceEnabled &&
                id == that.getId() &&
                guildId == that.guildId &&
                name == that.name
    }


}
