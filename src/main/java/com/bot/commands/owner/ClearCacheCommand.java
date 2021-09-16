package com.bot.commands.owner;

import com.bot.caching.GuildCache;
import com.bot.caching.MarkovModelCache;
import com.bot.caching.R34Cache;
import com.bot.caching.SubredditCache;
import com.bot.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.springframework.stereotype.Component;

@Component
public class ClearCacheCommand extends OwnerCommand {
    private MarkovModelCache markovModelCache;
    private R34Cache r34Cache;
    private GuildCache guildCache;
    private SubredditCache subredditCache;

    public ClearCacheCommand(MarkovModelCache markovModelCache, R34Cache r34Cache,
                             GuildCache guildCache, SubredditCache subredditCache) {
        this.name = "clearcache";
        this.markovModelCache = markovModelCache;
        this.r34Cache = r34Cache;
        this.guildCache = guildCache;
        this.subredditCache = subredditCache;
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        markovModelCache.removeAll();
        logger.info("Cleared markov cache");

        r34Cache.removeAll();
        logger.info("Cleared r34 cache");

        guildCache.removeAll();
        logger.info("Cleared guild cache");

        subredditCache.removeAll();
        logger.info("Cleared subreddit cache");
    }
}
