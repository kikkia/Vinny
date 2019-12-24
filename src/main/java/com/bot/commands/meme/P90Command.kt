package com.bot.commands.meme

import com.bot.commands.MemeCommand
import com.bot.db.ChannelDAO
import com.bot.models.InternalTextChannel
import com.bot.utils.HttpUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.PrivateChannel

class P90Command : MemeCommand() {

    private val channelDAO: ChannelDAO

    init {
        this.name = "webm"
        this.help = "Gets a webm from P90.zone"
        this.arguments = "<Search terms or nothing>"
        this.aliases = arrayOf("p90")
        this.cooldown = 1
        this.cooldownScope = Command.CooldownScope.USER

        channelDAO = ChannelDAO.getInstance()
    }

    override fun executeCommand(commandEvent: CommandEvent) {
        val channel = channelDAO.getTextChannelForId(commandEvent.channel.id, true)
        var canNSFW = false
        if (channel == null) {
            channelDAO.addTextChannel(commandEvent.textChannel)
        } else {
            canNSFW = canNSFW(commandEvent, channel)
        }

        if (!canNSFW && !commandEvent.args.isEmpty()) {
            commandEvent.replyWarning("You cannot search without nsfw enabled for this channel " + "(Both in Vinny and discord).")
            return
        }

        try {
            commandEvent.reply(HttpUtils.getRandomP90Post(canNSFW, commandEvent.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]))
        } catch (e: Exception) {
            logger.severe("Issue getting p90 post!", e)
            commandEvent.replyError("There was an error getting a post.")
        }

    }

    private fun canNSFW(commandEvent: CommandEvent, channel: InternalTextChannel): Boolean {
        return if (commandEvent.channel is PrivateChannel) {
            true
        } else {
            // If no channel try to add it and keep going (not nsfw)
            channel.isNSFWEnabled && commandEvent.textChannel.isNSFW
        }
    }
}
