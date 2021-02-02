package com.bot.tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandTaskExecutor {
    private static ExecutorService taskExecutor = Executors.newCachedThreadPool();

    public static ExecutorService getTaskExecutor() {
        return taskExecutor;
    }
}
