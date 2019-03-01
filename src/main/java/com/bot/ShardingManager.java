package com.bot;

import com.bot.commands.general.InviteCommand;
import com.bot.commands.general.PingCommand;
import com.bot.commands.general.ShardStatsCommand;
import com.bot.commands.reddit.NewPostCommand;
import com.bot.commands.reddit.RandomPostCommand;
import com.bot.commands.reddit.TopPostCommand;
import com.bot.commands.settings.*;
import com.bot.commands.voice.*;
import com.bot.models.InternalShard;
import com.bot.utils.Config;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ShardingManager {

    private static final Logger LOGGER = Logger.getLogger(ShardingManager.class.getName());
    private static ShardingManager instance;

    private Map<Integer, InternalShard> shards;

    public static ShardingManager getInstance() {
        return instance;
    }

    public static ShardingManager getInstance(int numShards, boolean useDB) throws Exception {
        if (instance == null)
            instance = new ShardingManager(numShards, useDB);
        return instance;
    }

    // This adds a connection for each shard. Shards make it more efficient. ~1000 servers to shards is ideal
    // supportScript disables commands. Useful for running a supportScript simultaneously while the bot is going on prod
    private ShardingManager(int numShards, boolean useDB) throws Exception {
        Config config = Config.getInstance();
        shards = new HashMap<>();
        Bot bot = null;
        CommandClient client = null;
        bot = new Bot(new EventWaiter());

        CommandClientBuilder commandClientBuilder = new CommandClientBuilder();
        commandClientBuilder.setPrefix("~");
        commandClientBuilder.setOwnerId(config.getConfig(Config.OWNER_ID));

        commandClientBuilder.addCommands(
                // Voice Commands
                new PlayCommand(bot),
                new PauseCommand(),
                new RepeatCommand(),
                new StopCommand(),
                new ResumeCommand(),
                new VolumeCommand(),
                new ListTracksCommand(),
                new SkipCommand(),

                // Battle Royale
                //new BattleRoyaleCommand(),

                // General Commands
                new InviteCommand(),
                new ShardStatsCommand(),
                new PingCommand(),

                // Reddit Commands
                new RandomPostCommand(),
                new TopPostCommand(),
                new NewPostCommand()
        );

        // Commands that rely on the DB (usually only turned off to test)
        // TODO: Make DB Mandatory
        if (useDB) {
            commandClientBuilder.addCommands(
                    new SaveMyPlaylistCommand(bot),
                    new ListMyPlaylistCommand(),
                    new LoadMyPlaylistCommand(bot),
                    new LoadGuildPlaylistCommand(bot),
                    new SaveGuildPlaylistCommand(bot),
                    new ListGuildPlaylistCommand(),

                    // Guild Settings Commands
                    new DefaultVolumeCommand(),
                    new GetSettingsCommand(),
                    new SetBaseRoleCommand(),
                    new SetModRoleCommand(),
                    new SetNSFWCommand(),
                    new SetVoiceRoleCommand(),
                    new EnableNSFWCommand(),
                    new DisableNSFWCommand()
            );
        }
        commandClientBuilder.setEmojis("\u2714", "\u2757", "\u274c");
        client = commandClientBuilder.build();

        for (int i = 0; i < numShards; i++) {
            JDA jda = new JDABuilder(AccountType.BOT)
                    .setToken(config.getConfig(Config.DISCORD_TOKEN))
                    .useSharding(i, numShards)
                    .build();

            jda.awaitReady();

            EventWaiter waiter = new EventWaiter();
            jda.addEventListener(waiter);
            jda.addEventListener(client);
            jda.addEventListener(bot);
            int shardId = jda.getShardInfo().getShardId();
            shards.put(shardId, new InternalShard(shardId, jda));

            System.out.println("Shard " + i + " built.");
        }
    }

    public Map<Integer, InternalShard> getShards() {
        return shards;
    }

}
