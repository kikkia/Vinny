package com.bot.utils;

import com.kikkia.dislog.api.DislogClient;
import com.kikkia.dislog.models.Log;
import com.kikkia.dislog.models.LogLevel;

import java.util.logging.Level;

/**
 * Wrapper around the slf4j logger that allows us to funnel logs to discord channels and log them normally.
 */
public class Logger {
    private final java.util.logging.Logger logger;
    private final String name;
    private final boolean channelLoggingEnabled;

    private DislogClient dislogClient;

    public Logger(String name) {
        Config config = Config.getInstance();
        logger = java.util.logging.Logger.getLogger(name);
        this.name = name;

        channelLoggingEnabled = Boolean.parseBoolean(config.getConfig(Config.ENABLE_LOGGING_CHANNELS));
        if (channelLoggingEnabled) {
            dislogClient = LoggerUtils.getClient();
        }
    }

    public void log(Level level, String s) {
        log(level, s, null);
    }

    public void log(Level level, String s, Exception e) {

        if (level == Level.SEVERE)
            logError(s, e);
        else if (level == Level.WARNING) {
            logWarn(s, e);
        } else {
            logInfo(s);
        }
    }

    private void logError(String s, Exception e) {
        sendDislogLog(s, LogLevel.ERROR, e);
        logger.log(Level.SEVERE, s, e);
    }

    private void logWarn(String s, Exception e) {
        sendDislogLog(s, LogLevel.WARN, e);
        logger.log(Level.WARNING, s, e);
    }

    private void logInfo(String s) {
        sendDislogLog(s, LogLevel.INFO, null);
        logger.info(s);
    }

    public void info(String s) {
        log(Level.INFO, s, null);
    }

    public void warning(String s) {
        warning(s, null);
    }

    public void warning(String s, Exception e) {
        log(Level.WARNING, s, e);
    }

    public void severe(String s, Exception e) {
        log(Level.SEVERE, s, e);
    }

    private void sendDislogLog(String s, LogLevel logLevel, Exception e) {
        if (channelLoggingEnabled) {
            Log log = new Log(s, logLevel, e);
            dislogClient.queueLog(log);
        }
    }
}
