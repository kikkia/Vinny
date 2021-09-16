package com.bot.commands.owner;

import com.bot.ShardingManager;
import com.bot.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Component;

@Component
public class InChannelCommand extends OwnerCommand {
    public InChannelCommand() {
        this.name = "inchannel";
    }


    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        ShardingManager shardingManager = ShardingManager.getInstance();

        try {
            JDA jda = shardingManager.getShardForChannel(commandEvent.getArgs());
            commandEvent.replySuccess("Found it in guild: " + jda.getGuildChannelById(commandEvent.getArgs()));
        } catch (Exception e) {
            commandEvent.replyError("Did not find channel");
        }
    }
}
