package com.bot.db.models

class ResumeAudioGuild(val guildId: String, val voiceChannelId: String, val textChannelId: String, val volume: Int,
                       val volumeLocked: Boolean, val tracks: List<ResumeAudioTrack>)