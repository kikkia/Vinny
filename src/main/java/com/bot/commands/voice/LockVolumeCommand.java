package com.bot.commands.voice;

import com.bot.commands.ModerationCommand;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;

public class LockVolumeCommand extends ModerationCommand {

    public LockVolumeCommand() {
        this.name = "lockvolume";
        this.aliases = new String[]{"lvolume", "lvol", "lockv"};
        this.help = "Locks the volume for the playing stream. (Mod required)";
    }

    @Override
    //@trace(operationName = "executeCommand", resourceName = "LockVolume")
    protected void executeCommand(CommandEvent commandEvent) {
        VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
        if (handler == null) {
            commandEvent.replyWarning("I am not connected to a voice channel.");
            return;
        }

        String message = handler.toggleVolumeLock() ? "Volume is now locked" : "Volume is now unlocked";

        commandEvent.replySuccess(message);
    }

}
