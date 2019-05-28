package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.voice.QueuedAudioTrack;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.Queue;

public class ShuffleCommand extends VoiceCommand {

    public ShuffleCommand() {
        this.name = "shuffle";
        this.help = "Shuffles the current track queue";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
        Queue<QueuedAudioTrack> queue = handler.getTracks();
        if (queue.size() < 2) {
            commandEvent.replyWarning("There are not enough tracks in your queue to shuffle them.");
            return;
        }

        QueuedAudioTrack[] trackArray = (QueuedAudioTrack[]) queue.toArray();
    }
}
