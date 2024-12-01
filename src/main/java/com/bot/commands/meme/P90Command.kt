package com.bot.commands.meme

import com.bot.commands.MemeCommand
import com.bot.utils.HttpUtils
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel

class P90Command : MemeCommand() {


    init {
        this.name = "webm"
        this.help = "Gets a webm from P90.zone"
        this.arguments = "<Search terms or nothing>"
        this.aliases = arrayOf("p90")

    }

    @Trace(operationName = "executeCommand", resourceName = "P90")
    override fun executeCommand(commandEvent: CommandEvent) {
        val canNSFW = canNSFW(commandEvent)

        if (!canNSFW && !commandEvent.args.isEmpty()) {
            commandEvent.replyWarning("You cannot search without nsfw enabled for this channel " + "(Both in Vinny and discord).")
            return
        }

        try {
            commandEvent.reply(HttpUtils.getRandomP90Post(canNSFW, commandEvent.args))
        } catch (e: IllegalArgumentException) {
            logger.warning("No results returned from P90")
            commandEvent.replyWarning("No results returned from P90.zone")
        }
        catch (e: Exception) {
            logger.severe("Issue getting p90 post!", e)
            commandEvent.replyError("There was an error getting a post.")
        }

    }

    private fun canNSFW(commandEvent: CommandEvent): Boolean {
        return if (commandEvent.channel is PrivateChannel) {
            true
        } else {
            // If no channel try to add it and keep going (not nsfw)
            commandEvent.textChannel.isNSFW
        }
    }
}
