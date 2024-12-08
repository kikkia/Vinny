package com.bot.utils

import com.jagrosh.jdautilities.command.Command

object CommandCategories {
    @JvmField
    val VOICE: Command.Category = Command.Category("voice")
    @JvmField
    val GENERAL: Command.Category = Command.Category("general")
    @JvmField
    val NSFW: Command.Category = Command.Category("nsfw")
    @JvmField
    val MODERATION: Command.Category = Command.Category("moderation")

    // Derivatives of General Category for more granularity
    @JvmField
    val REDDIT: Command.Category = Command.Category("reddit")
    val MEME: Command.Category = Command.Category("meme")
    val SUBSCRIPTION: Command.Category = Command.Category("subscription")

    @JvmField
    val OWNER: Command.Category = Command.Category("owner")
}
