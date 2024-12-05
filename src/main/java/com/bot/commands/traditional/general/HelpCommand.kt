package com.bot.commands.traditional.general

import com.bot.Bot
import com.bot.commands.traditional.GeneralCommand
import com.bot.utils.ConstantStrings
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.CooldownScope
import datadog.trace.api.Trace
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import java.util.function.Consumer

class HelpCommand : GeneralCommand() {
    init {
        this.name = "help"
        this.aliases = arrayOf("commands", "command")
        this.cooldown = 5
        this.cooldownScope = CooldownScope.USER
    }

    @Trace(operationName = "executeCommand", resourceName = "help")
    override fun executeCommand(commandEvent: CommandEvent) {
        commandEvent.author.openPrivateChannel().queue(OpenSuccessConsumer(commandEvent), FailureConsumer(commandEvent))
    }

    class OpenSuccessConsumer(val commandEvent: CommandEvent) : Consumer<PrivateChannel> {
        override fun accept(t: PrivateChannel) {
            t.sendMessage("To learn in depth about all of Vinny's commands please go to this detailed document: " + ConstantStrings.COMMANDS_URL).queue(
                DMSuccessConsumer(commandEvent), FailureConsumer(commandEvent)
            )
            t.sendMessage("For extra support please join the support server " + Bot.SUPPORT_INVITE_LINK).queue()
        }
    }

    class DMSuccessConsumer(val commandEvent: CommandEvent) : Consumer<Message> {
        override fun accept(t: Message) {
            commandEvent.replySuccess(ConstantStrings.randomHelpSuccess)
        }
    }

    class FailureConsumer(val commandEvent: CommandEvent): Consumer<Throwable> {
        override fun accept(t: Throwable) {
            commandEvent.reply("To learn in depth about all of Vinny's commands please go to this detailed document: " + ConstantStrings.COMMANDS_URL)
            commandEvent.reply("For extra support please join the support server " + Bot.SUPPORT_INVITE_LINK)
        }
    }
}