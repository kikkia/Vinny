package com.bot;

import com.bot.commands.*;
import com.jagrosh.jdautilities.commandclient.CommandClient;
import com.jagrosh.jdautilities.commandclient.CommandClientBuilder;
import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

public class ShardingManager {

    private static JDA[] shards;

    // This adds a connection for each shard. Shards make it more efficient. ~1000 servers to shards is ideal
    public ShardingManager(int numShards, Config config) throws Exception{
        shards = new JDA[numShards];
        Bot bot = new Bot(new EventWaiter());

        CommandClientBuilder commandClientBuilder = new CommandClientBuilder();
        commandClientBuilder.useDefaultGame();
        commandClientBuilder.setPrefix("~");
        commandClientBuilder.setOwnerId(config.getToken("OwnerID"));

        commandClientBuilder.addCommands(new TestCommand(),
                new PlayCommand(bot),
                new PauseCommand(),
                new RepeatCommand(),
                new StopCommand());
        commandClientBuilder.setEmojis("\u2714", "\u2757", "\u274c");
        CommandClient client = commandClientBuilder.build();

        for (int i = 0; i < numShards; i++){
            shards[i] = new JDABuilder(AccountType.BOT)
                    .setToken(config.getToken("Discord"))
                    .useSharding(i, numShards)
                    .buildBlocking();

            EventWaiter waiter = new EventWaiter();

            shards[i].addEventListener(waiter);
            shards[i].addEventListener(client);
            shards[i].addEventListener(bot);
            System.out.println("Shard " + i + " built.");
        }
    }

    public static JDA[] getShards() {
        return shards;
    }

    public JDA getJDA(int shardId) {
        return shards[shardId];
    }
}
