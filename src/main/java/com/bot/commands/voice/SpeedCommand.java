package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import org.springframework.stereotype.Component;

@Component
public class SpeedCommand extends VoiceCommand {

    public SpeedCommand() {
        this.name = "speed";
        this.help = "Changes the playback speed of an audio stream. E.G 1.5 = 1.5x normal speed";
        this.arguments = "<0.1 - 2.0>";
        this.hidden = true;
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "Speed")
    protected void executeCommand(CommandEvent commandEvent) {
        VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
        double newSpeed;
        try {
            if (handler == null) {
                commandEvent.replyWarning("I am not connected to a voice channel.");
                return;
            }

            newSpeed = Double.parseDouble(commandEvent.getArgs().split(" ")[0]);
            if (newSpeed > 2.0 || newSpeed < 0.1) {
                throw new NumberFormatException();
            }
            if (handler.isLocked()) {
                commandEvent.replyWarning("Volume and speed is currently locked. You need to unlock it to edit it.");
                return;
            }

            handler.setSpeed(newSpeed);

            commandEvent.reactSuccess();
        }
        catch (NumberFormatException e) {
            commandEvent.replyWarning("You can enter a speed between 0.1 and 2.0 to set.\nCurrent speed is: " + handler.getSpeed());
        }
    }
}
