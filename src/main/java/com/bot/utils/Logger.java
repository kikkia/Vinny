package com.bot.utils;

import com.bot.ShardingManager;
import com.bot.models.InternalShard;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Map;
import java.util.logging.Level;

/**
 * Wrapper around the slf4j logger that allows us to funnel logs to discord channels and log them normally.
 */
public class Logger {
    private java.util.logging.Logger logger;
    private String name;
    private boolean channelLoggingEnabled;
    private boolean initialized;

    // Stored just until we initialize (allows us to statically make loggers)
    private String debugChannelId;
    private String errorChannelId;

    // Grabbed during initialization
    private TextChannel debugChannel;
    private TextChannel errorChannel;

    public Logger(String name) {
        Config config = Config.getInstance();
        logger = java.util.logging.Logger.getLogger(name);
        this.name = name;

        channelLoggingEnabled = Boolean.parseBoolean(config.getConfig(Config.ENABLE_LOGGING_CHANNELS));
        debugChannelId = config.getConfig(Config.DEBUG_CHANNEL_ID);
        errorChannelId = config.getConfig(Config.ERROR_CHANNEL_ID);

        initialized = false;
    }

    // Called when the first log is sent through
    private void init() {
        // To account for race conditions, if sharding manager is still null. Then ignore it;
        if (ShardingManager.getInstance() == null)
            return;

        // Get a list of all of the shards, since the channels could be on any one.
        Map<Integer, InternalShard> shards = ShardingManager.getInstance().getShards();

        // Check the shard for either one, keep in mind its possible that the channels could be on different servers/shards
        for (InternalShard s : shards.values()) {
            if (debugChannel == null) {
                debugChannel = s.getJda().getTextChannelById(debugChannelId);
            }
            if (errorChannel == null) {
                errorChannel = s.getJda().getTextChannelById(errorChannelId);
            }
            if (errorChannel != null && debugChannel != null) {
                // If we found them both then stop looking
                break;
            }
        }
        if (errorChannel != null || debugChannel != null) {
            // If one is found we know that we can get channels but the other is just setup wrong. It will be ignored in logic
            initialized = true;
        }
    }

    public void log(Level level, String s) {
        log(level, s, null);
    }

    public void log(Level level, String s, Exception e) {
        if (!initialized)
            init();

        if (level == Level.SEVERE)
            logError(s, e);
        else if (level == Level.WARNING) {
            logWarn(s);
        } else {
            logInfo(s);
        }
    }

    private void logError(String s, Exception e) {
        postToErrorChannel(s, e);
        logger.log(Level.SEVERE, s, e);
    }

    private void logWarn(String s) {
        postToDebugChannel("`WARN`\n" + s);
        logger.log(Level.WARNING, s);
    }

    private void logInfo(String s) {
        postToDebugChannel("`INFO`\n" + s);
        logger.info(s);
    }

    public void info(String s) {
        log(Level.INFO, s, null);
    }

    public void warning(String s) {
        log(Level.WARNING, s, null);
    }

    public void severe(String s, Exception e) {
        log(Level.SEVERE, s, e);
    }

    private void postToErrorChannel(String s, Exception e) {
        try {
            if (errorChannel != null) {
                errorChannel.sendMessage("`Error:`\n" + s).queue();
                if (e != null)
                    errorChannel.sendMessage("`Exception:`\n```" + e.toString() + "```").queue();
                errorChannel.sendMessage("StackTrace: ```" + ExceptionUtils.getStackTrace(e) + "```").queue();
            }
        } catch (IllegalStateException ex) {
            // Could be thrown when JDA gets rid of the channel object
            errorChannel = null;
            init();
            postToDebugChannel("Reinitializing error logging channel");
            postToErrorChannel(s, e);
        }
    }

    private void postToDebugChannel(String s) {
        try {
            if (debugChannel != null) {
                debugChannel.sendMessage(s).queue();
            }
        } catch (IllegalStateException e) {
            // Could be thrown when jda gets rid of cached channel
            debugChannel = null;
            init();
            postToDebugChannel("Reinitializing the debug logger");
            postToDebugChannel(s);
        }
    }
}
