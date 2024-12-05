package com.bot.commands.traditional

import com.bot.utils.CommandCategories
import net.dv8tion.jda.api.Permission
import java.util.*

abstract class RedditCommand : BaseCommand() {
    init {
        this.category = CommandCategories.REDDIT
        this.guildOnly = false
        this.botPermissions = arrayOf(Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS)
        this.canSchedule = true
        this.arguments = "<subreddit name> (optional --post-only)"
    }

    fun parseArgs(args: String) : String {
        var toReturn = args.lowercase(Locale.getDefault())
        if (args.contains("--post-only")) {
            toReturn = toReturn.replace("--post-only", "")
        }
        return toReturn.trim()
    }
}
