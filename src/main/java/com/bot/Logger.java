package com.bot;

import java.util.HashMap;
import java.util.Map;

public class Logger {

    private static Map<String, Logger> loggerMap;
    private String name;

    private Logger(String name) {
        this.name = name;
    }

    public static Logger getInstance(String name) {
        if (loggerMap == null)
            loggerMap = new HashMap<>();
        return loggerMap.computeIfAbsent(name, n -> new Logger(name));
    }

    public void INFO(String message) {
        System.out.println(name + " INFO: " + message);
    }

    public void WARNING(String message) {
        System.out.println(name + " WARNING: " + message);
    }

    public void ERROR(String message) {
        System.out.println(name + " ERROR: " + message);
    }

    public void DEBUG(String message) {
        System.out.println(name + " DEBUG: " + message);
    }

    public void WOOWEEE(String message) {
        System.out.println(name + " WOOWEE: " + message);
    }
}
