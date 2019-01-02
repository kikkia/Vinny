package com.bot;

import com.bot.commands.general.InviteCommand;
import com.bot.commands.reddit.NewPostCommand;
import com.bot.commands.reddit.RandomPostCommand;
import com.bot.commands.reddit.TopPostCommand;
import com.bot.commands.settings.*;
import com.bot.commands.voice.*;
import com.bot.commands.battleroyale.BattleRoyaleCommand;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

import java.util.logging.Logger;

public class ShardingManager {

    private static final Logger LOGGER = Logger.getLogger(ShardingManager.class.getName());

    private JDA[] shards;

    // This adds a connection for each shard. Shards make it more efficient. ~1000 servers to shards is ideal
    // supportScript disables commands. Useful for running a supportScript simultaneously while the bot is going on prod
    public ShardingManager(int numShards, boolean supportScript, boolean useDB) throws Exception {
        Config config = Config.getInstance();
        shards = new JDA[numShards];
        Bot bot = null;
        CommandClient client = null;

        if (!supportScript) {
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

                    // Battle Royale
                    new BattleRoyaleCommand(),

                    // General Commands
                    new InviteCommand(),

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
                        new SetVoiceRoleCommand()
                );
            }

            commandClientBuilder.setEmojis("\u2714", "\u2757", "\u274c");


            client = commandClientBuilder.build();
        }

        for (int i = 0; i < numShards; i++){
            shards[i] = new JDABuilder(AccountType.BOT)
                    .setToken(config.getConfig(Config.DISCORD_TOKEN))
                    .useSharding(i, numShards)
                    .buildBlocking();
            if (!supportScript) {
                EventWaiter waiter = new EventWaiter();

                shards[i].addEventListener(waiter);
                shards[i].addEventListener(client);
                shards[i].addEventListener(bot);
            }

            System.out.println("Shard " + i + " built.");
        }
    }

    public JDA[] getShards() {
        return shards;
    }

    public JDA getJDA(int shardId) {
        return shards[shardId];
    }
}
