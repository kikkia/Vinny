package com.bot.commands.meme

import com.bot.commands.MemeCommand
import com.github.lalyos.jfiglet.FigletFont
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import org.springframework.stereotype.Component

import java.io.IOException

@Component
open class AsciiCommand : MemeCommand() {
    init {
        this.name = "ascii"
        this.help = "Generates a figlet of the text its given"
        this.arguments = "<Text to make ascii>"
    }

    @Trace(operationName = "executeCommand", resourceName = "Ascii")
    override fun executeCommand(commandEvent: CommandEvent) {
        if (commandEvent.args.length > 500) {
            commandEvent.reply(commandEvent.client.warning + " Please keep the input to under 500 characters.")
            return
        }
        try {
            val ascii = FigletFont.convertOneLine(commandEvent.args)
            commandEvent.reply("```$ascii```")
        } catch (e: IOException) {
            commandEvent.reply(commandEvent.client.error + " Something went wrong. Please try again.")
            logger.severe("Error generating ascii: ", e)
            metricsManager.markCommandFailed(this, commandEvent.author, commandEvent.guild)
        } catch (e: Exception) {
            commandEvent.reply(commandEvent.client.warning + "Failed to generate ascii. Make sure you are only using unicode characters.")
        }

    }
}
