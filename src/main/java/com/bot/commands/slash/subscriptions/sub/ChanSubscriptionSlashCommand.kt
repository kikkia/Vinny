package com.bot.commands.slash.subscriptions.sub

import com.bot.commands.slash.ExtSlashCommandEvent
import com.bot.models.RssProvider
import com.bot.utils.ChanUtils.Companion.getBoard
import com.bot.utils.ConstantStrings
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.sql.SQLException

class ChanSubscriptionSlashCommand: AddSubscriptionSlashCommand() {

    init {
        name = "4chan"
        help = "Get notified when a new thread gets posted on a 4chan board"
        options = listOf(
            OptionData(OptionType.STRING, "board", "The 4chan board to watch", true),
            OptionData(OptionType.CHANNEL, "channel", "Channel to post in")
        )
    }

    override fun runCommand(command: ExtSlashCommandEvent) {
        val subject = command.optString("board")
        val board = getBoard(subject!!)
        if (board == null) {
            command.replyWarning(ConstantStrings.CHAN_BOARD_INVALID)
            return
        } else if (board.nsfw && !command.channel.asTextChannel().isNSFW) {
            command.replyWarning(ConstantStrings.CHAN_BOARD_NSFW)
            return
        }
        val subChannel = getEffectiveChannel(command)

        try {
            rssDAO.addSubscription(
                RssProvider.CHAN,
                subject,
                subChannel.id,
                command.user.id,
                board.nsfw
            )
        } catch (e: SQLException) {
            logger.severe("Error adding chan sub", e)
            command.replyError("Something went wrong adding the subscription, please try again.")
            return
        }
        command.replySuccess(ConstantStrings.CHAN_SUB_SUCCESS)
    }
}