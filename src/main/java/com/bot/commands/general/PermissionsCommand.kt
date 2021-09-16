package com.bot.commands.general

import com.bot.ShardingManager
import com.bot.commands.GeneralCommand
import com.bot.exceptions.ForbiddenCommandException
import com.bot.exceptions.PermsOutOfSyncException
import com.bot.utils.CommandPermissions
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.jagrosh.jdautilities.menu.Paginator
import datadog.trace.api.Trace
import net.dv8tion.jda.api.entities.User
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
open class PermissionsCommand(val waiter: EventWaiter) : GeneralCommand() {

    private val builder: Paginator.Builder

    init {
        this.name = "perms"
        this.help = "Gets all permissions for the user in the current server"
        this.arguments = "<User ID> or nothing for self"
        this.guildOnly = true
        this.aliases = arrayOf("perm", "permissions", "permission")

        builder = Paginator.Builder()
                .setColumns(1)
                .setItemsPerPage(10)
                .useNumberedItems(false)
                .showPageNumbers(true)
                .setEventWaiter(waiter)
                .setTimeout(30, TimeUnit.SECONDS)
                .waitOnSinglePage(false)
                .setFinalAction { message -> message.clearReactions().queue() }
    }

    @Trace(operationName = "executeCommand", resourceName = "Permissions")
    override fun executeCommand(commandEvent: CommandEvent) {

        val userId: Long
        val user: User?
        // If the args are not blank we will parse them
        if (!commandEvent.args.isEmpty()) {
            try {
                userId = java.lang.Long.parseLong(commandEvent.args)
                user = commandEvent.jda.getUserById(userId)
            } catch (e: Exception) {
                commandEvent.replyWarning("There was a problem parsing the userID. Please make sure it is a valid ID.")
                return
            }

        } else {
            user = commandEvent.author
        }

        val member = commandEvent.guild.getMember(user!!)

        if (member == null) {
            commandEvent.replyWarning(user.name + "#" + user.discriminator + " is not on this server.")
            return
        }

        builder.setText("Permissions for " + member.effectiveName)
                .setUsers(commandEvent.author)
                .setColor(commandEvent.selfMember.color)
        val perms = ArrayList<String>()

        for (p in member.permissions) {
            perms.add(p.getName())
        }

        // Logic to fill out the list, so that vinny permissions get their own page
        val size = perms.size
        for (i in 0 until 10 - size % 10) {
            perms.add(" ")
        }

        // Now add in vinny permissions
        val shardingManager = ShardingManager.getInstance()
        perms.add("Vinny Permissions")
        for (c in shardingManager.commandCategories) {
            val b = StringBuilder()
            try {
                if (CommandPermissions.canExecuteCommand(c, commandEvent)) {
                    b.append(":white_check_mark:")
                } else {
                    b.append(":x:")
                }
            } catch (e: ForbiddenCommandException) {
                b.append(":x:")
            } catch (e: PermsOutOfSyncException) {
                commandEvent.replyError("Could not find the role required for " + c.name + " commands. Please have the owner of the server set a new role or reset the roles with the `~reset` command")
            }

            b.append(c.name)
            perms.add(b.toString())
        }

        builder.setItems(*perms.toTypedArray())

        builder.build().paginate(commandEvent.channel, 1)
    }
}
