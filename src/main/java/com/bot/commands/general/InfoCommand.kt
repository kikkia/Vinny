package com.bot.commands.general

import com.bot.Bot
import com.bot.ShardingManager
import com.bot.commands.GeneralCommand
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import net.dv8tion.jda.api.EmbedBuilder

class InfoCommand : GeneralCommand() {
    init {
        this.name = "info"
        this.help = "Information about Vinny"
        this.aliases = arrayOf("vinny", "about")
    }

    @Trace(operationName = "executeCommand", resourceName = "Info")
    override fun executeCommand(commandEvent: CommandEvent) {
        val manager = ShardingManager.getInstance()

        val builder = EmbedBuilder()
        builder.setImage(commandEvent.selfUser.avatarUrl)
        builder.setTitle("Vinny\n" + manager.totalGuilds + " Servers")
        val desc = "Vinny is an open-source discord bot written in Java. Vinny is completely free and community driven. Spicing up your discord server has never been easier."
        builder.setDescription(desc)
        val support = "To report bugs, give feedback, request commands, or just say hi, you can find the Vinny support server here: " + Bot.SUPPORT_INVITE_LINK
        builder.addField("Want to suggest a command?", support, false)
        val inv = "Vinny can be added to any server you admin/own by following the link given by the `~invite` command"
        builder.addField("Want to invite Vinny to a server?", inv, false)
        builder.setFooter("Owner: Kikkia#3782", "https://cdn.discordapp.com/avatars/124988914472583168/7a55ecbd57ee85cf168c3ed30f8fb446.png")

        commandEvent.reply(builder.build())
    }
}
