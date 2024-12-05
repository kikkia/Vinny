package com.bot.commands.traditional.meme

import com.bot.commands.traditional.MemeCommand
import com.bot.utils.CommandPermissions
import com.bot.utils.HttpUtils
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace

class P90Command : MemeCommand() {


    init {
        this.name = "webm"
        this.help = "Gets a webm from P90.zone"
        this.arguments = "<Search terms or nothing>"
        this.aliases = arrayOf("p90")

    }

    @Trace(operationName = "executeCommand", resourceName = "P90")
    override fun executeCommand(commandEvent: CommandEvent) {
        val canNSFW = CommandPermissions.allowNSFW(commandEvent)

        if (!canNSFW && commandEvent.args.isNotEmpty()) {
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
}
