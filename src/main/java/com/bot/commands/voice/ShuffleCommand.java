package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;

public class ShuffleCommand extends VoiceCommand {

    public ShuffleCommand() {
        this.name = "shuffle";
        this.help = "Shuffles the current voice queue";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
        if (handler == null)
            return;
        try {
            handler.shuffleTracks();
            commandEvent.reactSuccess();
        } catch (Exception e) {
            commandEvent.replyError("Failed to shuffle tracks");
            logger.severe("Failed to shuffle tracks", e);
        }

    }
}
