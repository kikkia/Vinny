package com.bot.commands.meme

import com.bot.commands.MemeCommand
import com.bot.utils.FormattingUtils
import com.bot.utils.SauceUtils
import com.jagrosh.jdautilities.command.CommandEvent

class SauceCommand : MemeCommand() {

    init {
        this.name = "sauce"
    }

    override fun executeCommand(commandEvent: CommandEvent?) {
        if (commandEvent?.message?.attachments?.isEmpty()!!) {
            commandEvent.replyWarning("Please attach something")
            return
        }
        val attatchment = commandEvent.message.attachments[0]
        if (!attatchment.isImage) {
            commandEvent.replyWarning("Can only get sauce of images")
            return
        }
        val sauceClient = SauceUtils.getClient()
        val sauce = sauceClient.getSauce(attatchment.url)
        commandEvent.reply(FormattingUtils.getEmbedForSauce(sauce))
    }
}