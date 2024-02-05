package com.bot.interactions.commands.general

import com.bot.ShardingManager
import com.bot.interactions.InteractionEvent
import com.bot.interactions.commands.GeneralCommand
import com.bot.utils.FormattingUtils
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User

class UserCommand : GeneralCommand() {

    init {
        this.name = "user"
        this.help = "Gives details about a user"
        this.arguments = "<userId> (No mentions) or nothing for self"
        this.aliases = arrayOf("userinfo", "uinfo")
    }

    @Trace(operationName = "executeCommand", resourceName = "User")
    override fun executeCommand(commandEvent: CommandEvent) {
        val shardingManager = ShardingManager.getInstance()

        val userId: Long
        val user: User?
        // If the args are not blank we will parse them
        if (!commandEvent.args.isEmpty()) {
            try {
                userId = java.lang.Long.parseLong(commandEvent.args)
            } catch (e: Exception) {
                commandEvent.replyWarning("There was a problem parsing the userID. Please make sure it is a valid ID.")
                return
            }

            user = shardingManager.getUserFromAnyShard(userId)
            if (user == null) {
                commandEvent.replyWarning("Could not find that user.")
                return
            }
        } else {
            user = commandEvent.author
        }

        val member = commandEvent.guild.getMember(user!!)

        val builder = EmbedBuilder()
        builder.setTitle(user.name + "#" + user.discriminator)
        builder.setImage(user.avatarUrl)
        builder.addField("Created:", FormattingUtils.formatOffsetDateTimeToDay(user.timeCreated), true)
        // User is in the server, make the embed more server specific
        if (member != null) {
            builder.addField("Joined server:", FormattingUtils.formatOffsetDateTimeToDay(member.timeJoined), true)
            builder.setDescription(FormattingUtils.getOnlineStatusEmoji(member) + member.asMention)

            // Build role list
            val roles = FormattingUtils.formattedRolesList(member)
            if (roles.isEmpty()) {
                builder.addField("Roles", "No roles", false)
            } else {
                builder.addField("Roles", roles, false)
            }
        } else {
            // User not in guild, use some more of the user info
            builder.setDescription("User is not in this server")
        }

        commandEvent.reply(builder.build())
    }

    override fun executeCommand(commandEvent: InteractionEvent?) {
        TODO("Not yet implemented")
    }
}
