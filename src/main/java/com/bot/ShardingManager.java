package com.bot;

import com.bot.commands.alias.AddGuildAliasCommand;
import com.bot.commands.alias.AliasesCommand;
import com.bot.commands.alias.RemoveGuildAliasCommand;
import com.bot.commands.general.*;
import com.bot.commands.meme.*;
import com.bot.commands.moderation.*;
import com.bot.commands.nsfw.E621Command;
import com.bot.commands.nsfw.R4cCommand;
import com.bot.commands.nsfw.Rule34Command;
import com.bot.commands.owner.*;
import com.bot.commands.reddit.NewPostCommand;
import com.bot.commands.reddit.RandomPostCommand;
import com.bot.commands.reddit.ShitpostCommand;
import com.bot.commands.reddit.TopPostCommand;
import com.bot.commands.rss.*;
import com.bot.commands.scheduled.GetScheduledCommand;
import com.bot.commands.scheduled.ScheduleCommand;
import com.bot.commands.scheduled.UnscheduleCommand;
import com.bot.commands.voice.*;
import com.bot.config.properties.DiscordProperties;
import com.bot.models.InternalShard;
import com.bot.preferences.GuildPreferencesManager;
import com.bot.voice.VoiceSendHandler;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.impl.CommandClientImpl;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

@Component
public class ShardingManager {

    private Map<Integer, InternalShard> shards;
    private final ScheduledExecutorService executor;
    public ShardManager shardManager;

    private EventWaiter waiter;
    private List<Command.Category> commandCategories;
    private CommandClient client;

    // This adds a connection for each shard. Shards make it more efficient. ~1000 servers to shards is ideal
    // supportScript disables commands. Useful for running a supportScript simultaneously while the bot is going on prod
    public ShardingManager(DiscordProperties discordProperties, Bot bot) throws Exception {
        waiter = new EventWaiter();

        shards = new ConcurrentHashMap<>();
        executor = Executors.newScheduledThreadPool(50);

        CommandClientBuilder commandClientBuilder = new CommandClientBuilder();
        commandClientBuilder.setPrefix(discordProperties.getPrefix());
        commandClientBuilder.setAlternativePrefix("@mention");
        commandClientBuilder.setOwnerId(discordProperties.getOwnerId());

        // If we are deploying silently we are not registering commands.
        if (!discordProperties.getSilent()) {
            commandClientBuilder.addCommands(
                    // Voice Commands
                    new PlayCommand(bot),
                    new SearchCommand(bot, waiter),
                    new NowPlayingCommand(),
                    new RemoveTrackCommand(bot),
                    new PauseCommand(),
                    new RepeatCommand(),
                    new StopCommand(),
                    new ResumeCommand(),
                    new VolumeCommand(),
                    new DefaultVolumeCommand(),
                    new ListTracksCommand(waiter),
                    new SkipCommand(),
                    new SaveMyPlaylistCommand(bot),
                    new ListMyPlaylistCommand(),
                    new LoadMyPlaylistCommand(bot),
                    new LoadGuildPlaylistCommand(bot),
                    new SaveGuildPlaylistCommand(bot),
                    new ListGuildPlaylistCommand(),
                    new ShuffleCommand(),
                    new ClearQueueCommand(),
                    new RemovePlaylistCommand(),
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
                    new SauceCommand(),

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
                    new RemoveSubscriptionCommand(),

                    // NSFW Commands
                    new Rule34Command(),
                    new E621Command(),

                    // 4chan commands
                    new R4cCommand()
            );
        }
        commandClientBuilder.useHelpBuilder(false);

        commandClientBuilder.addCommands(
                // Owner Commands -- All hidden
                new AvatarCommand(),
                new UpdateGuildCountCommand(),
                new ClearCacheCommand(),
                new RebootAnnounceCommand(),
                new GuildDebugCommand(),
                new SwitchDefaultSearchCommand(),
                new InGuildCommand(),
                new InChannelCommand(),
                new FUserCommand(),
                new ForceSendMessage(),
                new ShardCommand(),
                new ShardStatsCommand(),
                new SetUsageCommand()
                );

        commandClientBuilder.setServerInvite("https://discord.gg/XMwyzxZ\nFull Command list with examples: " +
                "https://github.com/JessWalters/Vinny-Redux/blob/master/docs/Commands.md");
        commandClientBuilder.setEmojis("\u2705", "\u2757", "\u274c");
        commandClientBuilder.setGuildSettingsManager(new GuildPreferencesManager());
        commandClientBuilder.setActivity(null);
        commandClientBuilder.setScheduleExecutor(executor);
        client = commandClientBuilder.build();

        shardManager = DefaultShardManagerBuilder
                .createDefault(
                        discordProperties.getToken(),
                        GUILD_MEMBERS,
                        GUILD_MESSAGES,
                        GUILD_EMOJIS,
                        GUILD_MESSAGE_REACTIONS,
                        GUILD_VOICE_STATES,
                        DIRECT_MESSAGES
                )
                .setShardsTotal(discordProperties.getTotalShards())
                .setChunkingFilter(ChunkingFilter.NONE)
                .setShards(discordProperties.getStartShard(), discordProperties.getEndShard())
                .setCompression(Compression.NONE)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(client, waiter, bot)
                .setAudioSendFactory(new NativeAudioSendFactory())
                .setActivity(null)
                .setRequestTimeoutRetry(true)
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

    public List<VoiceSendHandler> getActiveVoiceSendHandlers() {
        List<VoiceSendHandler> activeHandlers = new ArrayList<>();

        for (InternalShard shard : shards.values()) {
            activeHandlers.addAll(shard.getVoiceSendHandlers().stream().filter(VoiceSendHandler::isActive).collect(Collectors.toList()));
        }
        return activeHandlers;
    }
}
