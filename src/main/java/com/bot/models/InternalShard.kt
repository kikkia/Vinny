package com.bot.models

import net.dv8tion.jda.api.JDA

class InternalShard(val jda: JDA) {
    val id: Int = jda.shardInfo.shardId

    val serverCount: Int
        get() = jda.guilds.size

    val userCount: Int
        get() = jda.users.size
}
