package com.bot.interactions.commands.owner;

import com.bot.ShardingManager;
import com.bot.interactions.InteractionEvent;
import com.bot.interactions.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

public class ShardCommand extends OwnerCommand {
    public ShardCommand() {
        this.name = "shardinfo";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        try {
            int i = Integer.parseInt(commandEvent.getArgs());
            ShardingManager manager = ShardingManager.getInstance();
            JDA shard = manager.getShards().get(i).getJda();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Info for shard: " + commandEvent.getArgs())
                    .addField("ping", shard.getGatewayPing() + "", true)
                    .addField("guilds", shard.getGuilds().size() + "", true)
                    .addField("users", shard.getUsers().size() + "", true)
                    .addField("Thread Pool Stats", "", false)
                    .addField("callback pool active", !shard.getCallbackPool().isShutdown() + "", true)
                    .addField("gateway pool active", !shard.getGatewayPool().isShutdown() + "", true)
                    .addField("ratelimit pool active", !shard.getRateLimitPool().isShutdown() + "", true)
                    .addField("http pool active", shard.getHttpClient().connectionPool().connectionCount() + "", true)
                    .addField("http pool idle", shard.getHttpClient().connectionPool().idleConnectionCount() + "", true);
            commandEvent.reply(builder.build());

        } catch (Exception e) {
            commandEvent.replyError("oof");
        }

    }

    @Override
    protected void executeCommand(InteractionEvent commandEvent) {

    }
}
