package com.bot.interactions.commands.meme

import com.bot.interactions.InteractionEvent
import com.bot.interactions.TextCommandInteraction
import com.bot.interactions.commands.MemeCommand
import com.github.lalyos.jfiglet.FigletFont
import com.jagrosh.jdautilities.command.CommandEvent
import datadog.trace.api.Trace
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

import java.io.IOException

class AsciiCommand : MemeCommand() {
    init {
        this.name = "ascii"
        this.help = "Generates a figlet of the text its given"
        this.arguments = "<Text to make ascii>"
        this.options.add(OptionData(OptionType.STRING, "text", "Text to change to ascii art", true))
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        executeCommand(TextCommandInteraction(commandEvent!!))
    }

    @Trace(operationName = "executeCommand", resourceName = "Ascii")
    override fun executeCommand(commandEvent: InteractionEvent) {
        if (commandEvent.getArgs().length > 500) {
            commandEvent.replyWarning(" Please keep the input to under 500 characters.")
            return
        }
        try {
            val ascii = FigletFont.convertOneLine(commandEvent.getArgs())
            commandEvent.reply("```$ascii```")
        } catch (e: IOException) {
            logger.severe("Error generating ascii: ", e)
            metricsManager.markCommandFailed(this, commandEvent.getUser(), commandEvent.getGuild())
            throw e
        } catch (e: Exception) {
            throw e
        }
    }
}
