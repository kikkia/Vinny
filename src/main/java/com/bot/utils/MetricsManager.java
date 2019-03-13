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
                new String[] {"tag:value"}            /* Datadog extension: Constant tags, always applied */
            );
    }

    public void markCommand(Command command, User user, Guild guild) {
        statsd.incrementCounter("category." + command.getCategory().getName());
        statsd.incrementCounter("user." + user.getId());
        statsd.incrementCounter("command." +command.getName());
        statsd.incrementCounter("guild." + guild.getId());
    }

    public void markCommandFailed(Command command, User user, Guild guild) {
        statsd.incrementCounter("category.failed." + command.getCategory().getName());
        statsd.incrementCounter("user.failed." + user.getId());
        statsd.incrementCounter("command.failed." +command.getName());
        statsd.incrementCounter("guild.failed." + guild.getId());
    }
}
