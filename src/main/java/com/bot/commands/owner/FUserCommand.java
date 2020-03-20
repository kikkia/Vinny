package com.bot.commands.owner;

import com.bot.ShardingManager;
import com.bot.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.User;

public class FUserCommand extends OwnerCommand {
    public FUserCommand() {
        this.name = "fuser";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        ShardingManager shardingManager = ShardingManager.getInstance();
        try {
            User user = shardingManager.getUserFromAnyShard(Long.parseLong(commandEvent.getArgs()));
            if (user != null)
                commandEvent.replySuccess("Found user " + user.getName());
        } catch (Exception e) {
            // Not found
        }
        commandEvent.replyError("Did not find user");
    }
}
