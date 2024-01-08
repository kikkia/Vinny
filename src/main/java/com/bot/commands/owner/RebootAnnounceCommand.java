package com.bot.commands.owner;

import com.bot.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class RebootAnnounceCommand extends OwnerCommand {


    public RebootAnnounceCommand() {
        this.name = "reboot";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
//        ShardingManager shardingManager = ShardingManager.getInstance();
//        for (VoiceSendHandler handler : shardingManager.getActiveVoiceSendHandlers()) {
//            handler.sendUpdateToLastUsedChannel(ConstantStrings.REBOOT_VOICE_MESSAGE);
//        }
        commandEvent.reactSuccess();
    }
}
