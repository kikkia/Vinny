package com.bot.commands.owner;

import com.bot.ShardingManager;
import com.bot.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.MessageChannel;

public class ForceSendMessage extends OwnerCommand {
    public ForceSendMessage() {
        this.name = "fsend";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        ShardingManager shardingManager = ShardingManager.getInstance();
        try {
            String channelId = commandEvent.getArgs().split(", ")[0];
            String message = commandEvent.getArgs().split(", ")[1];
            MessageChannel channel = shardingManager.getShardForChannel(channelId).getTextChannelById(channelId);
            assert channel != null;
            channel.sendMessage(message).queue();
        } catch (Exception e) {
            commandEvent.replyError("Failed to send due to exception");
            commandEvent.replyError(e.getMessage());
        }
    }
}
