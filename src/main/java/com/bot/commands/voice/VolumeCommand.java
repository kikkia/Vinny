package com.bot.commands.voice;

import com.bot.commands.VoiceCommand;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.CommandEvent;

public class VolumeCommand extends VoiceCommand {

	public VolumeCommand() {
		this.name = "volume";
		this.arguments = "<Volume 1-200>";
		this.help = "Sets the players volume";
	}

	@Override
	protected void executeCommand(CommandEvent commandEvent) {
		VoiceSendHandler handler = (VoiceSendHandler) commandEvent.getGuild().getAudioManager().getSendingHandler();
		int newVolume;
		try {
			if (handler == null) {
				commandEvent.replyWarning("I am not connected to a voice channel.");
				return;
			}

			newVolume = Integer.parseInt(commandEvent.getArgs().split(" ")[0]);
			if (newVolume > 200 || newVolume < 0) {
				throw new NumberFormatException();
			}
			if (handler.isLocked()) {
				commandEvent.replyWarning("Volume is currently locked. You need to unlock it to edit it.");
				return;
			}
			handler.getPlayer().setVolume(newVolume);
			commandEvent.reactSuccess();
		}
		catch (NumberFormatException e) {
			commandEvent.replyWarning("You can enter a volume between 0 and 200 to set.\nCurrent volume: " + handler.getPlayer().getVolume());
		}
	}
}
