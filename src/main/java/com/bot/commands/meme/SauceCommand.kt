package com.bot.commands.meme

import com.bot.commands.MemeCommand
import com.bot.utils.FormattingUtils
import com.bot.utils.SauceUtils
import com.jagrosh.jdautilities.command.CommandEvent
import com.kikkia.jsauce.models.exceptions.NoSauceFoundException
import com.kikkia.jsauce.models.exceptions.TooMuchSauceException
import datadog.trace.api.Trace
import java.util.*
import java.util.regex.Pattern


class SauceCommand : MemeCommand() {
    private var urlP: Pattern = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",Pattern.CASE_INSENSITIVE)

    init {
        this.name = "sauce"
        this.canSchedule = false
        this.help = "Finds the source of an attached or linked image"
        this.arguments = "<attached or linked image>"
        this.cooldown = 15
        this.cooldownScope = CooldownScope.GUILD
        this.aliases = Collections.singletonList("source").toTypedArray()
    }

    //@trace(operationName = "executeCommand", resourceName = "Sauce")
    override fun executeCommand(commandEvent: CommandEvent?) {
        var url: String? = null

        if (!commandEvent?.args.isNullOrEmpty()) {
            val matcher = urlP.matcher(commandEvent?.args)
            if (matcher.matches())
                url = matcher.group(0)

        }

        if (url == null) {
            if (commandEvent?.message?.attachments?.isEmpty()!!) {
                commandEvent.replyWarning("Please attach something")
                return
            }
            val attachment = commandEvent.message.attachments[0]
            if (!attachment.isImage) {
                commandEvent.replyWarning("Can only get sauce of images")
                return
            }
            url = attachment.url
        }

        val sauceClient = SauceUtils.getClient()

        try {
            val sauce = sauceClient.getSauce(url!!)
            commandEvent?.reply(FormattingUtils.getEmbedForSauce(sauce))
        } catch (e : TooMuchSauceException) {
            commandEvent?.replyError("I am experiencing high amounts of traffic searching for a sauce. Please try again later.")
            logger.severe("Ratelimit hit getting sauce", e)
        } catch (e : NoSauceFoundException) {
            commandEvent?.replyError("No sauce was found for the given image :(")
            logger.warning("No results found for sauce", e)
        } catch (e : Exception) {
            commandEvent?.replyError("Something went wrong getting the sauce")
            logger.severe("Exception encountered getting sauce", e)
        }
    }
}