package com.bot.commands.owner;

import com.bot.ShardingManager;
import com.bot.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class GuildDebugCommand extends OwnerCommand {

    ShardingManager shardingManager;

    public GuildDebugCommand() {
        this.name = "gdbug";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        shardingManager = ShardingManager.getInstance();
        String guildId = commandEvent.getArgs();

        try {
            if (shardingManager.getShardForGuild(guildId) == null) {
                commandEvent.reactWarning();
            } else {
                commandEvent.reactSuccess();
            }
        } catch (Exception e) {
            commandEvent.reactError();
        }
    }
}
