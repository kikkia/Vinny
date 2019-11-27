package com.bot;

import com.bot.commands.alias.AddGuildAliasCommand;
import com.bot.commands.alias.AliasesCommand;
import com.bot.commands.alias.RemoveGuildAliasCommand;
import com.bot.commands.general.*;
import com.bot.commands.meme.*;
import com.bot.commands.nsfw.R4cCommand;
import com.bot.commands.nsfw.Rule34Command;
import com.bot.commands.owner.AvatarCommand;
import com.bot.commands.owner.ClearCacheCommand;
import com.bot.commands.owner.UpdateGuildCountCommand;
import com.bot.commands.reddit.NewPostCommand;
import com.bot.commands.reddit.RandomPostCommand;
import com.bot.commands.reddit.ShitpostCommand;
import com.bot.commands.reddit.TopPostCommand;
import com.bot.commands.scheduled.GetScheduledCommand;
import com.bot.commands.scheduled.ScheduleCommand;
import com.bot.commands.settings.*;
import com.bot.commands.voice.*;
import com.bot.models.InternalShard;
import com.bot.preferences.GuildPreferencesManager;
import com.bot.utils.Config;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.impl.CommandClientImpl;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ShardingManager {

    private static ShardingManager instance;

    private Map<Integer, InternalShard> shards;
    private ScheduledExecutorService executor;
    public ShardManager shardManager;

    private EventWaiter waiter;
    private List<Command.Category> commandCategories;
    private CommandClient client;

    public static ShardingManager getInstance() {
        return instance;
    }

    public static ShardingManager getInstance(int numShards, int startIndex, int endIndex) throws Exception {
        if (instance == null)
            instance = new ShardingManager(numShards, startIndex, endIndex);
        return instance;
    }

    // This adds a connection for each shard. Shards make it more efficient. ~1000 servers to shards is ideal
    // supportScript disables commands. Useful for running a supportScript simultaneously while the bot is going on prod
    private ShardingManager(int numShards, int startIndex, int endIndex) throws Exception {
        Config config = Config.getInstance();
        waiter = new EventWaiter();

        // Check if we are just doing a silent deploy (For debug and stress testing purposes)
        boolean silentDeploy = Boolean.parseBoolean(config.getConfig(Config.SILENT_DEPLOY));

        shards = new HashMap<>();
        executor = Executors.newScheduledThreadPool(10);
        Bot bot = new Bot();

        CommandClientBuilder commandClientBuilder = new CommandClientBuilder();
        commandClientBuilder.setPrefix("~");
        commandClientBuilder.setAlternativePrefix("@mention");
        commandClientBuilder.setOwnerId(config.getConfig(Config.OWNER_ID));

        // If we are deploying silently we are not registering commands.
        if (!silentDeploy) {
            commandClientBuilder.addCommands(
                    // Voice Commands
                    new PlayCommand(bot),
                    new SearchCommand(bot, waiter),
                    new NowPlayingCommand(),
                    new PauseCommand(),
                    new RepeatCommand(),
                    new StopCommand(),
                    new ResumeCommand(),
                    new VolumeCommand(),
                    new DefaultVolumeCommand(),
                    new ListTracksCommand(),
                    new SkipCommand(),
                    new SaveMyPlaylistCommand(bot),
                    new ListMyPlaylistCommand(),
                    new LoadMyPlaylistCommand(bot),
                    new LoadGuildPlaylistCommand(bot),
                    new SaveGuildPlaylistCommand(bot),
                    new ListGuildPlaylistCommand(),
                    new LockVolumeCommand(),

                    // Battle Royale
                    //new BattleRoyaleCommand(),

                    // General Commands
                    new InfoCommand(),
                    new SayCommand(),
                    new VoteCommand(),
                    new InviteCommand(),
                    new SupportCommand(),
                    new StatsCommand(),
                    new ShardStatsCommand(),
                    new PingCommand(),
                    new GetSettingsCommand(),
                    new PrefixesCommand(),
                    new RollCommand(),
                    new UserCommand(),
                    new PermissionsCommand(waiter),
                    new ServerInfoCommand(),
                    new GamesCommand(waiter),
                    new PixivCommand(),

                    // Alias Commands
                    new AliasesCommand(waiter),
                    new ScheduleCommand(waiter),
                    new GetScheduledCommand(waiter),

                    // Meme Commands
                    new P90Command(),
                    new CommentCommand(),
                    new AsciiCommand(),
                    new ShitpostCommand(),
                    new ClapCommand(),
                    new CopyPastaCommand(),

                    // Reddit Commands
                    new RandomPostCommand(),
                    new TopPostCommand(),
                    new NewPostCommand(),

                    // Guild Settings Commands
                    new SetBaseRoleCommand(),
                    new SetModRoleCommand(),
                    new SetNSFWCommand(),
                    new SetVoiceRoleCommand(),
                    new ToggleNSFWCommand(true),
                    new ToggleNSFWCommand(false),
                    new AddPrefixCommand(),
                    new RemovePrefixCommand(),
                    new AddGuildAliasCommand(waiter),
                    new RemoveGuildAliasCommand(),

                    // NSFW Commands
                    new Rule34Command(),

                    // 4chan commands
                    new R4cCommand()
            );
        } else {
            commandClientBuilder.useHelpBuilder(false);
        }

        // Owner commands are added regardless
        commandClientBuilder.addCommands(
                // Owner Commands -- All hidden
                new AvatarCommand(),
                new UpdateGuildCountCommand(),
                new ClearCacheCommand()
        );

        commandClientBuilder.setServerInvite("https://discord.gg/XMwyzxZ\nFull Command list with examples: " +
                "https://github.com/JessWalters/Vinny-Redux/blob/master/docs/Commands.md");
        commandClientBuilder.setEmojis("\u2714", "\u2757", "\u274c");
        commandClientBuilder.setGuildSettingsManager(new GuildPreferencesManager());
        commandClientBuilder.setActivity(null);
        commandClientBuilder.setScheduleExecutor(executor);
        client = commandClientBuilder.build();

        shardManager = new DefaultShardManagerBuilder()
                .setToken(config.getConfig(Config.DISCORD_TOKEN))
                .setShardsTotal(numShards)
                .setShards(startIndex, endIndex)
                .addEventListeners(client, waiter, bot)
                .setActivity(null)
                .setContextEnabled(false)
                .build();
    }

    public Map<Integer, InternalShard> getShards() {
        return shards;
    }

    public void putShard(InternalShard shard) {shards.put(shard.getId(), shard);}

    public ScheduledExecutorService getExecutor() {return executor;}

    public int getTotalGuilds() {
        int guilds = 0;
        for (InternalShard shard : shards.values()) {
            guilds += shard.getServerCount();
        }
        return guilds;
    }

    public CommandClientImpl getCommandClientImpl() {
        if (client instanceof CommandClientImpl)
            return (CommandClientImpl) client;
        else
            return null;
    }

    public List<Command.Category> getCommandCategories() {
        if (commandCategories == null) {
            commandCategories = new ArrayList<>();
            for (Command c: client.getCommands()) {
                if (c.getCategory() == null)
                    continue;
                if (!commandCategories.contains(c.getCategory())) {
                    commandCategories.add(c.getCategory());
                }
            }
        }
        return commandCategories;
    }

    public JDA getShardForGuild(String guildId) {
        for (InternalShard shard : shards.values()) {
            if (shard.getJda().getGuildById(guildId) != null) {
                return shard.getJda();
            }
        }
        return null;
    }
}
