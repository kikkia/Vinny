package com.bot.tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandTaskExecutor {
    private static ExecutorService taskExecutor = Executors.newFixedThreadPool(400);
    private static ExecutorService scheduledCommandExecutor = Executors.newFixedThreadPool(5);

    public static ExecutorService getTaskExecutor() {
        return taskExecutor;
    }

    public static ExecutorService getScheduledCommandExecutor() {
        return scheduledCommandExecutor;
    }
}
