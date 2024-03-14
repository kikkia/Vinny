package com.bot.tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandTaskExecutor {
    private static final ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private static final ExecutorService scheduledCommandExecutor = Executors.newFixedThreadPool(6);

    public static ExecutorService getTaskExecutor() {
        return taskExecutor;
    }

    public static ExecutorService getScheduledCommandExecutor() {
        return scheduledCommandExecutor;
    }
}
