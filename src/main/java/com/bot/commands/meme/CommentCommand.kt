package com.bot.commands.meme

import com.bot.caching.MarkovModelCache
import com.bot.commands.MemeCommand
import com.bot.models.MarkovModel
import com.bot.utils.ConstantStrings
import com.bot.utils.HttpUtils
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.CooldownScope
import datadog.trace.api.Trace
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import java.awt.Color
import java.util.*

class CommentCommand : MemeCommand() {

    private val markovCache: MarkovModelCache

    init {
        this.name = "comment"
        this.help = "Generates a comment from the post history of a user or a channel"
        this.arguments = "<@user or userID> or <#channel>"
        this.cooldownScope = CooldownScope.USER
        this.cooldown = 2
        this.botPermissions = arrayOf(Permission.MESSAGE_HISTORY, Permission.MESSAGE_SEND)
        this.canSchedule = false

        markovCache = MarkovModelCache.getInstance()
    }

    @Trace(operationName = "executeCommand", resourceName = "Comment")
    override fun executeCommand(commandEvent: CommandEvent) {
        val mentionedUsers = ArrayList(commandEvent.message.mentions.users)

        // In case the user is using the @ prefix, then get rid of the bot in the list.
        if (mentionedUsers.contains(commandEvent.selfMember.user)) {
            mentionedUsers.removeAt(0)
        }

        val user: User?
        var markov: MarkovModel?
        if (mentionedUsers.isEmpty() && commandEvent.message.mentions.channels.isEmpty()) {
            // Try to get the user with a userid
            if (commandEvent.args.isNotEmpty()) {
                try {
                    user = commandEvent.jda.getUserById(commandEvent.args)
                    if (user == null) {
                        // Just throw so it goes to the catch
                        throw NumberFormatException()
                    }
                } catch (e: NumberFormatException) {
                    commandEvent.reply(commandEvent.client.warning + " you must either mention a user or give their userId.")
                    return
                }

            } else {
                commandEvent.reply(commandEvent.client.warning + " you must either mention a user or give their userId.")
                return
            }
        } else if (mentionedUsers.isEmpty() && commandEvent.message.mentions.channels.isNotEmpty()) {
            getMarkovForChannel(commandEvent)
            return
        } else {
            user = mentionedUsers[0]
        }

        // See if we have the model cached. If so we can skip rebuilding it. (We user server+user so that it does not go across servers)
        markov = markovCache.get(commandEvent.guild.id + user!!.id)

        if (markov == null) {
            // Throw an entry into the cache to denote a cache building
            markovCache.put(commandEvent.guild.id + user.id, MarkovModel())
            // No cached model found. Make a new one.
            val message = commandEvent.channel.sendMessage("No cached markov model found for user. I am building one. This will take a bit.").complete()

            markov = MarkovModel()

            // Fill the model with messages from all channels who have the right author
            try {
                for (t in commandEvent.guild.textChannels) {
                    if (t.canTalk(commandEvent.guild.getMember(user)!!) && commandEvent.selfMember.hasPermission(t, Permission.MESSAGE_HISTORY)) {
                        var msg_limit = 2000
                        for (m in t.iterableHistory.cache(false)) {
                            // Check that message is the right author and has content.
                            if (m.author.id == user.id && m.contentRaw.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size > 1)
                                markov.addPhrase(m.contentRaw)

                            // After 1000, break
                            if (--msg_limit <= 0)
                                break
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logger.severe("Issue generating comment", e)
                commandEvent.replyError("I failed to generate a model for them due to an error. Please try again later.")
                markovCache.remove(commandEvent.guild.id + user.id)
            }

            // Cache it
            markovCache.put(commandEvent.guild.id + user.id, markov)
            message.delete().queue()
        } else {
            if (markov.wordCount == 0) {
                commandEvent.replyWarning(ConstantStrings.MARKOV_GENERATING)
                return
            }
        }
        sendComment(commandEvent, markov, user, null)
    }

    private fun getMarkovForChannel(commandEvent: CommandEvent) {
        val channel = commandEvent.message.mentions.channels[0] as MessageChannel

        // See if we have the model cached. If so we can skip rebuilding it.
        var markov: MarkovModel? = markovCache.get(channel.id)

        if (markov == null) {
            // Placeholder for generation
            markovCache.put(channel.id, MarkovModel())

            // No cached model found. Make a new one.
            commandEvent.channel.sendMessage("No cached markov model found for channel. " +
                    "I am building one. This will take a little bit.").complete()

            markov = MarkovModel()

            // Fill the model with messages from a given channel
            try {
                var msgLimit = 5000
                if (commandEvent.selfMember.hasPermission(channel as GuildChannel, Permission.MESSAGE_HISTORY)) {
                    for (m in channel.iterableHistory.cache(false)) {
                        // Check that message is the right author and has content.
                        if (m.contentRaw.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size > 1)
                            markov.addPhrase(m.contentRaw)

                        // After 1000, break
                        if (--msgLimit <= 0)
                            break
                    }
                } else {
                    commandEvent.replyWarning("I need the `MESSAGE_HISTORY` permission for that channel to generate a comment.")
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logger.severe("Issue generating comment", e)
                commandEvent.replyError("I failed to generate a model for them due to an error. Please try again later.")
                markovCache.remove(channel.id)
            }

            // Cache it
            markovCache.put(channel.id, markov)
        } else {
            if (markov.wordCount == 0) {
                commandEvent.replyWarning(ConstantStrings.MARKOV_GENERATING)
                return
            }
        }
        sendComment(commandEvent, markov, null, channel)
    }

    // Sends a formatted comment for a channel or a user
    private fun sendCommentEmbed(commandEvent: CommandEvent, markovModel: MarkovModel, user: User?, channel: MessageChannel?) {
        val builder = EmbedBuilder()
        if (user != null)
            builder.setAuthor(user.name, user.avatarUrl, user.avatarUrl)

        if (channel != null)
            builder.setAuthor(channel.name, commandEvent.guild.iconUrl, commandEvent.guild.iconUrl)

        builder.setFooter("Messages: " + markovModel.messageCount + "  -  Words: " + markovModel.wordCount +
                "\nFor a more realistic comment, give me the MANAGE_WEBHOOKS permission.", null)

        var phrase = markovModel.phrase
        if (phrase.length > 1020) {
            phrase = phrase.substring(0, 1018) + "."
        }

        builder.addField("", phrase, false)
        builder.setColor(Color(0, 255, 0))
        commandEvent.reply(builder.build())
    }

    private fun sendComment(commandEvent: CommandEvent, markovModel: MarkovModel, user: User?, channel: MessageChannel?) {
        if (commandEvent.isFromType(ChannelType.TEXT) && commandEvent.selfMember.hasPermission(commandEvent.textChannel as GuildChannel, Permission.MANAGE_WEBHOOKS)) {
            val hooks = commandEvent.textChannel.retrieveWebhooks().complete()

            // If there are webhooks, lets send that way
            var vinnyHook = hooks.stream().filter{ it.name == "vinny" }.findFirst()
            if (!vinnyHook.isPresent) {
                vinnyHook = Optional.of(commandEvent.textChannel.createWebhook("vinny").complete())
            }

            if (user != null) {
                HttpUtils.sendCommentHook(vinnyHook.get(), markovModel, commandEvent.guild.getMember(user), null)
            } else {
                HttpUtils.sendCommentHook(vinnyHook.get(), markovModel, null, channel)
            }
        } else {
            sendCommentEmbed(commandEvent, markovModel, user, channel)
        }
    }
}
