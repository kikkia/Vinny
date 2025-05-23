package com.bot;

import com.bot.commands.ButtonInteractionListener;
import com.bot.commands.SelectMenuInteractionHandler;
import com.bot.commands.slash.general.InviteSlashCommand;
import com.bot.commands.slash.general.SupportSlashCommand;
import com.bot.commands.slash.nsfw.E621SlashCommand;
import com.bot.commands.slash.nsfw.R34SlashCommand;
import com.bot.commands.slash.reddit.RedditSlashCommand;
import com.bot.commands.slash.subscriptions.MySubscriptionsSlashCommand;
import com.bot.commands.slash.subscriptions.SubscribeCommand;
import com.bot.commands.slash.subscriptions.SubscriptionsSlashCommand;
import com.bot.commands.slash.subscriptions.UnsubscribeSlashCommand;
import com.bot.commands.slash.voice.*;
import com.bot.commands.traditional.alias.AddGuildAliasCommand;
import com.bot.commands.traditional.alias.AliasesCommand;
import com.bot.commands.traditional.alias.RemoveGuildAliasCommand;
import com.bot.commands.traditional.general.*;
import com.bot.commands.traditional.meme.*;
import com.bot.commands.traditional.moderation.*;
import com.bot.commands.traditional.nsfw.E621Command;
import com.bot.commands.traditional.nsfw.PixivNSFWCommand;
import com.bot.commands.traditional.nsfw.R4cCommand;
import com.bot.commands.traditional.nsfw.Rule34Command;
import com.bot.commands.traditional.owner.*;
import com.bot.commands.traditional.reddit.NewPostCommand;
import com.bot.commands.traditional.reddit.RandomPostCommand;
import com.bot.commands.traditional.reddit.ShitpostCommand;
import com.bot.commands.traditional.reddit.TopPostCommand;
import com.bot.commands.traditional.rss.*;
import com.bot.commands.traditional.scheduled.GetScheduledCommand;
import com.bot.commands.traditional.scheduled.ScheduleCommand;
import com.bot.commands.traditional.scheduled.UnscheduleCommand;
import com.bot.commands.traditional.voice.*;
import com.bot.models.InternalShard;
import com.bot.preferences.GuildPreferencesManager;
import com.bot.utils.VinnyConfig;
import com.bot.voice.CustomJDAVoiceUpdateListener;
import com.bot.voice.LavaLinkClient;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.impl.CommandClientImpl;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

public class ShardingManager {

    private static ShardingManager instance;

    private final Map<Integer, InternalShard> shards;
    private final ScheduledExecutorService executor;
    public ShardManager shardManager;

    private final EventWaiter waiter;
    private List<Command.Category> commandCategories;
    private final CommandClient client;

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
        VinnyConfig config = VinnyConfig.Companion.instance();
        waiter = new EventWaiter();

        // Check if we are just doing a silent deploy (For debug and stress testing purposes)
        boolean silentDeploy = config.getBotConfig().getSilentDeploy();

        shards = new ConcurrentHashMap<>();
        executor = Executors.newScheduledThreadPool(100);
        Bot bot = new Bot();

        CommandClientBuilder commandClientBuilder = new CommandClientBuilder();
        commandClientBuilder.setPrefix("~");
        commandClientBuilder.setAlternativePrefix("@mention");
        commandClientBuilder.setOwnerId(config.getDiscordConfig().getOwnerId());

        // If we are deploying silently we are not registering commands.
        if (!silentDeploy) {
            commandClientBuilder.addCommands(
                    // Voice Commands
                    new PlayCommand(),
                    new SearchCommand(bot, waiter),
                    new NowPlayingCommand(),
                    new RemoveTrackCommand(),
                    new PauseCommand(),
                    new RepeatCommand(),
                    new StopCommand(),
                    new VolumeCommand(),
                    new DefaultVolumeCommand(),
                    new ListTracksCommand(waiter),
                    new SkipCommand(),
                    new SaveMyPlaylistCommand(bot),
                    new ListMyPlaylistCommand(),
                    new LoadMyPlaylistCommand(),
                    new LoadGuildPlaylistCommand(),
                    new SaveGuildPlaylistCommand(bot),
                    new ListGuildPlaylistCommand(),
                    new ShuffleCommand(),
                    new ClearQueueCommand(),
                    new RemovePlaylistCommand(),
                    new SeekCommand(),
                    new FastForwardCommand(),
                    new RewindCommand(),
                    new AutoplayCommand(),
                    new LoginDeviceCommand(),
                    new LogoutDeviceCommand(),
                    new RefreshTokenCommand(),
                    new LofiCommand(),
                    new MyTokenCommand(),
                    //new SpeedCommand(),

                    // Battle Royale
                    //new BattleRoyaleCommand(),

                    // General Commands
                    new InfoCommand(),
                    new SayCommand(),
                    new VoteCommand(),
                    new InviteCommand(),
                    new SupportCommand(),
                    new StatsCommand(),
                    new PingCommand(),
                    new GetSettingsCommand(),
                    new PrefixesCommand(),
                    new RollCommand(),
                    new UserCommand(),
                    new PermissionsCommand(waiter),
                    new ServerInfoCommand(),
                    new GamesCommand(waiter),
                    new PixivCommand(),
                    new ReviewCommand(),
                    new GetScheduledCommand(waiter),
                    new ListSubscriptionsCommand(waiter),
                    new HelpCommand(),
                    new PremiumServerCommand(),

                    // Alias Commands
                    new AliasesCommand(waiter),

                    // Meme Commands
                    new P90Command(),
                    new CommentCommand(),
                    new AsciiCommand(),
                    new ShitpostCommand(),
                    new ClapCommand(),
                    new CopyPastaCommand(),
                    new KickRouletteCommand(waiter),
                    new MemeKickCommand(waiter),

                    // Reddit Commands
                    new RandomPostCommand(),
                    new TopPostCommand(),
                    new NewPostCommand(),

                    // Guild Settings Commands
                    new SetBaseRoleCommand(),
                    new SetModRoleCommand(),
                    new SetNSFWCommand(),
                    new SetVoiceRoleCommand(),
                    new AddPrefixCommand(),
                    new RemovePrefixCommand(),
                    new AddGuildAliasCommand(waiter),
                    new RemoveGuildAliasCommand(),
                    new LockVolumeCommand(),
                    new ScheduleCommand(waiter),
                    new UnscheduleCommand(waiter),
                    new PurgeCommand(),
                    new SubscribeChanCommand(waiter),
                    new SubscribeTwitterCommand(waiter),
                    new SubscribeRedditCommand(waiter),
                    new SubscriptionsCommand(waiter),
                    new SubscribeTwitchCommand(waiter),
                    new SubscribeYoutubeCommand(waiter),
                    new SubscribeBlueskyCommand(waiter),
                    new RemoveSubscriptionCommand(),

                    // NSFW Commands
                    new Rule34Command(),
                    new E621Command(),
                    new PixivNSFWCommand(),

                    // 4chan commands
                    new R4cCommand()
            );
        }
        commandClientBuilder.useHelpBuilder(false);

        commandClientBuilder.addCommands(
                // Owner Commands -- All hidden
                new AvatarCommand(),
                new ClearCacheCommand(),
                new RebootAnnounceCommand(),
                new TestRebootAnnounceCommand(),
                new GuildDebugCommand(),
                new InGuildCommand(),
                new InChannelCommand(),
                new FUserCommand(),
                new ForceSendMessage(),
                new ShardCommand(),
                new ShardStatsCommand(),
                new SetUsageCommand(),
                new BanImageCommand(),
                new SetPixivSessionCommand(),
                new ThreadDumpCommand(),
                new ManualUpsertCommand(),
                new ForceDefaultSearchCommand(),
                new TestLofiCommand());

        commandClientBuilder.addSlashCommands(new SubscribeCommand(),
                new SubscriptionsSlashCommand(waiter),
                new MySubscriptionsSlashCommand(waiter),
                new UnsubscribeSlashCommand(),
                new RedditSlashCommand(),
                new R34SlashCommand(),
                new E621SlashCommand(),
                new SupportSlashCommand(),
                new InviteSlashCommand(),
                new PlaySlashCommand(),
                new LoadPlaylistSlashCommand(),
                new SavePlaylistSlashCommand(),
                new ListPlaylistsSlashCommand(waiter),
                new AutoplaySlashCommand(),
                new LoginSlashCommand(),
                new LogoutSlashCommand(),
                new RefreshTokenSlashCommand(),
                new DefaultVolumeSlashCommand(),
                new SeekSlashCommand(),
                new VolumeSlashCommand(),
                new MoveTrackSlashCommand(),
                new PlayNextSlashCommand(),
                new ListTracksSlashCommand(waiter),
                new LofiSlashCommand(),
                new DeletePlaylistSlashCommand());

        commandClientBuilder.setServerInvite("https://discord.gg/XMwyzxZ\nFull Command list with examples: " +
                "https://github.com/kikkia/Vinny-Redux/blob/master/docs/Commands.md");
        commandClientBuilder.setEmojis("\u2705", "\u2757", "\u274c");
        commandClientBuilder.setGuildSettingsManager(new GuildPreferencesManager());
        commandClientBuilder.setActivity(null);
        commandClientBuilder.setScheduleExecutor(executor);
        commandClientBuilder.setManualUpsert(false);
        client = commandClientBuilder.build();

        shardManager = DefaultShardManagerBuilder
                .createDefault(
                        config.getDiscordConfig().getToken(),
                        GUILD_MEMBERS,
                        GUILD_MESSAGES,
                        GUILD_EMOJIS_AND_STICKERS,
                        GUILD_MESSAGE_REACTIONS,
                        GUILD_VOICE_STATES,
                        DIRECT_MESSAGES,
                        MESSAGE_CONTENT
                )
                .setShardsTotal(numShards)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setShards(startIndex, endIndex)
                .setCompression(Compression.NONE)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(client,
                        waiter,
                        bot,
                        new ButtonInteractionListener(),
                        new SelectMenuInteractionHandler())
                .setActivity(null)
                .setRequestTimeoutRetry(true)
                .setContextEnabled(true)
                .enableCache(CacheFlag.VOICE_STATE)
                .setVoiceDispatchInterceptor(new CustomJDAVoiceUpdateListener(
                        new JDAVoiceUpdateListener(LavaLinkClient.Companion.getInstance().getClient())))
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

    public JDA getShardForChannel(String channelId) {
        for (InternalShard shard : shards.values()) {
            if (shard.getJda().getGuildChannelById(channelId) != null) {
                return shard.getJda();
            }
        }
        return null;
    }

    public GuildChannel getChannel(String channelId) {
        for (InternalShard shard : shards.values()) {
            GuildChannel channel = shard.getJda().getGuildChannelById(channelId);
            if (channel != null) {
                return channel;
            }
        }
        return null;
    }

    public User getUserFromAnyShard(Long userId) {
        return getUserFromAnyShard("" + userId);
    }

    public User getUserFromAnyShard(String userId) {
        for (InternalShard shard : shards.values()) {
            User user = shard.getJda().getUserById(userId);
            if (user != null) {
                return user;
            }
        }
        return null;
    }
}
