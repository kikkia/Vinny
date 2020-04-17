package com.bot.commands.moderation

import com.bot.commands.ModerationCommand
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import java.time.OffsetDateTime
import java.util.stream.Collectors

class PurgeCommand() : ModerationCommand() {
    val helpLink = "https://github.com/kikkia/Vinny/blob/master/docs/purge.md"
    val twoWeekWarning = " NOTE: I cannot purge messages that are older than 2 weeks old."

    init {
        this.name = "purge"
        this.arguments = "<arguments> <number 2-1000>"
        this.help = "For advanced usage please see the commands link at the bottom of the help message"
        this.botPermissions = listOf(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY).toTypedArray()
        this.guildOnly = true
        this.cooldown = 10
        this.cooldownScope = CooldownScope.USER_GUILD
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        if (commandEvent!!.args.isBlank()) {
            commandEvent.replyWarning("Incorrect command usage, please see $helpLink for additional help.")
            return
        }

        // Create filter
        val filter = MessageFilter.fromArgs(commandEvent.args)

        if (filter.numToDelete > 1000 || filter.numToDelete < 2) {
            commandEvent.replyWarning("Invalid count to delete, you can only delete between 2 and 1000 messages at a time.")
            return
        }

        val toDelete = ArrayList<Message>()

        var twoWeeksAgo = commandEvent.message.timeCreated.minusHours(335)
        var twoWeekWarn = false
        commandEvent.async {
            val messages = ArrayList<Message>()
            val messageHistory = commandEvent.channel.history
            var queriedCount = 0

            while (queriedCount < 2000) {
                val queried = messageHistory.retrievePast(100).complete()
                queriedCount += 100
                toDelete.addAll(queried.stream().filter{ filter.shouldDelete(it, twoWeeksAgo)}.collect(Collectors.toList()))

                if (toDelete.size == filter.numToDelete)
                    break

                if (queried.size < 100)
                    break // out of messages
                if (queried[99].timeCreated.isBefore(twoWeeksAgo)) {
                    twoWeekWarn = true
                    break
                }
            }

            if (toDelete.isEmpty()) {
                commandEvent.replyWarning("I found no messages to delete." + (if (twoWeekWarn) twoWeekWarning else ""))
                return@async
            }

            var delCount = 0
            try {
                while (delCount < toDelete.size) {
                    if ((toDelete.size - delCount) == 1) {
                        toDelete[toDelete.size - 1].delete().complete()
                        delCount++
                    }
                    else {
                        commandEvent.textChannel.deleteMessages(toDelete.subList(delCount,
                                (delCount + 100).coerceAtMost(toDelete.size))).complete()
                        delCount += 100
                    }
                }
            } catch (e: Exception) {
                logger.severe("Failed to purge some messages", e)
                commandEvent.replyError("Failed to purge ${toDelete.size - delCount} messages. " + (if (twoWeekWarn) twoWeekWarning else ""))
                return@async
            }
            // Removed by request
            // commandEvent.replySuccess("Successfully purged ${toDelete.size} messages." + (if (twoWeekWarn) twoWeekWarning else ""))
        }
    }

    internal class MessageFilter(val users: List<String>,
                                 val prefixes: List<String>,
                                 val contains: List<String>,
                                 val botOnly: Boolean,
                                 val numToDelete: Int) {

        private var numDeleted = 0

        fun shouldDelete(message: Message, time: OffsetDateTime) : Boolean {
            if (numDeleted >= numToDelete)
                return false
            if (message.isPinned) {
                return false
            } else if (botOnly && !message.author.isBot && prefixes.isEmpty()) {
                return false
            } else if (users.isNotEmpty() && !users.contains(message.author.id)) {
                return false
            }

            var matchesPrefix = prefixes.isEmpty() || (botOnly && message.author.isBot) // allows bots to trigger prefix match always when doing bot only
            for (prefix in prefixes) {
                if (message.contentRaw.startsWith(prefix)) {
                    matchesPrefix = true
                    break
                }
            }
            var matchesContent = contains.isEmpty()
            for (content in contains) {
                if (message.contentRaw.contains(content)) {
                    matchesContent = true
                    break
                }
            }

            if (matchesContent && matchesPrefix) {
                numDeleted++
                return !message.timeCreated.isBefore(time) // Two week ago check
            }
            return false
        }

        companion object {
            val PREFIX_REGEX = Regex("prefix=[\"“”](.*?)[\"“”]", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
            val CONTENT_REGEX = Regex("[\"“”](.*?)[\"“”]", RegexOption.DOT_MATCHES_ALL)
            val ID_REGEX = Regex("\\b(\\d{17,22})\\b")
            val MENTION_REGEX = Regex("<@!?(\\d{17,22})>")
            val COUNT_REGEX = Regex("\\b(\\d{1,4})\\b")

            fun fromArgs(arguments: String) : MessageFilter {
                var args = arguments.toLowerCase()

                val prefixes = ArrayList<String>()

                for (match in PREFIX_REGEX.findAll(args))
                    prefixes.add(match.groupValues[1].trim())

                args = args.replace(PREFIX_REGEX, " ")

                val contentMatches = ArrayList<String>()
                for (match in CONTENT_REGEX.findAll(args))
                    contentMatches.add(match.groupValues[1].trim())
                args = args.replace(CONTENT_REGEX, " ")

                val userIds = ArrayList<String>()
                for (match in MENTION_REGEX.findAll(args))
                    userIds.add(match.groupValues[1])
                args = args.replace(MENTION_REGEX, " ")

                for (match in ID_REGEX.findAll(args))
                    userIds.add(match.groupValues[1])
                args = args.replace(ID_REGEX, " ")

                var count = 10
                if (COUNT_REGEX.containsMatchIn(args))
                    count = Integer.parseInt(COUNT_REGEX.find(args)!!.groupValues[1].trim())

                return MessageFilter(userIds, prefixes, contentMatches, args.contains("bot"), count)
            }
        }
    }

    class countFilter
}