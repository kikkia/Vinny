package com.bot.commands.owner;

import com.bot.caching.GuildCache;
import com.bot.caching.MarkovModelCache;
import com.bot.caching.R34Cache;
import com.bot.caching.SubredditCache;
import com.bot.commands.OwnerCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class ClearCacheCommand extends OwnerCommand {
    public ClearCacheCommand() {
        this.name = "clearcache";
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        MarkovModelCache markovModelCache = MarkovModelCache.getInstance();
        markovModelCache.removeAll();
        logger.info("Cleared markov cache");

        R34Cache r34Cache = R34Cache.getInstance();
        r34Cache.removeAll();
        logger.info("Cleared r34 cache");

        GuildCache guildCache = GuildCache.getInstance();
        guildCache.removeAll();
        logger.info("Cleared guild cache");

        SubredditCache subredditCache = SubredditCache.getInstance();
        subredditCache.removeAll();
        logger.info("Cleared subreddit cache");
    }
}
