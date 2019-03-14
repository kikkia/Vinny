package com.bot.utils;

import com.jagrosh.jdautilities.command.Command;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

public class MetricsManager {

    // TODO: Health checks
    private final StatsDClient statsd;
    private static MetricsManager instance;

    public static MetricsManager getInstance() {
        if (instance == null) {
            instance = new MetricsManager();
        }
        return instance;
    }

    private MetricsManager() {
         statsd = new NonBlockingStatsDClient(
                "vinny-redux.live",                          /* prefix to any stats; may be null or empty string */
                "localhost",                        /* common case: localhost */
                8125,                                 /* port */
                new String[] {"vinny:live"}            /* Datadog extension: Constant tags, always applied */
            );
    }

    public void markCommand(Command command, User user, Guild guild) {
        String userTag = "user:" + user.getId();
        String commandTag = "command:" + command.getName();
        String categoryTag = "category:" + command.getCategory().getName();

        // Support guild being null (use in PMs)
        String guildOrPM = guild == null ? "PM" : guild.getId();
        String guildTag = "guild:" + guildOrPM;

        statsd.incrementCounter("command", userTag, guildTag, commandTag, categoryTag);
    }

    public void markCommandFailed(Command command, User user, Guild guild) {
        String userTag = "user:" + user.getId();
        String guildTag = "guild:" + guild.getId();
        String commandTag = "command:" + command.getName();
        String categoryTag = "category:" + command.getCategory().getName();
        statsd.incrementCounter("command.failed", userTag, guildTag, commandTag, categoryTag);
    }

    public void updateCacheSize(int count, int limit) {
        statsd.recordGaugeValue("cache.size", count);
        statsd.recordGaugeValue("cache.max", limit);
    }

    public void markCacheHit() {
        statsd.incrementCounter("cache.hit");
    }

    public void markCacheMiss() {
        statsd.incrementCounter("cache.miss");
    }
}
