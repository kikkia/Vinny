package com.bot.interactions.commands.owner;

import com.bot.ShardingManager;
import com.bot.interactions.InteractionEvent;
import com.bot.interactions.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.JDA;

public class InGuildCommand extends OwnerCommand {

    public InGuildCommand() {
        this.name = "inguild";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        ShardingManager shardingManager = ShardingManager.getInstance();
        try {
            JDA jda = shardingManager.getShardForGuild(commandEvent.getArgs());
            commandEvent.replySuccess("Its here on shard" + jda.getShardInfo().getShardId());
        } catch (Exception e) {
            commandEvent.replyError("Nope");
        }
    }

    @Override
    protected void executeCommand(InteractionEvent commandEvent) {

    }
}
