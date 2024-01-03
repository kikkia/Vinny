package com.bot.commands.voice

import com.bot.commands.VoiceCommand
import com.bot.voice.LLLoadHandler
import com.bot.voice.LavaLinkClient
import com.jagrosh.jdautilities.command.CommandEvent

class PlayLLCommand : VoiceCommand() {
    val client = LavaLinkClient.getInstance()
    init {
        name = "playLL"
    }

    override fun executeCommand(commandEvent: CommandEvent) {
        val guild = commandEvent.guild
        if (commandEvent.args.isEmpty()) {
            commandEvent.replyWarning("You need to provide something to play")
        }
        if (!guild.selfMember.voiceState!!.inVoiceChannel()) {
            client.joinEventChannel(commandEvent);
        }
        val gClient = client.getLink(guild.idLong)
        client.loadTrack(gClient, commandEvent.args, commandEvent)
    }
}