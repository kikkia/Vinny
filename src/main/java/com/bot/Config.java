package com.bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    private static Config config = null;
    private final File configFile;
    private Map<String, String> configs;

    public static final String DISCORD_TOKEN = "DISCORD_TOKEN";
    public static final String REDDIT_TOKEN = "REDDIT_TOKEN";
    public static final String REDDIT_CLIENT_ID = "REDDIT_CLIENT_ID";
    public static final String OWNER_ID = "OWNER_ID";
    public static final String DISCORD_BOT_ID = "BOT_ID";
    public static final String BOTLIST_API_TOKEN = "BOTLIST_API";
    public static final String BOT_API_TOKEN = "BOT_PW_API";


    public static final String USE_DB = "USE_DB";
    public static final String DB_URI = "DB_URI";
    public static final String DB_USERNAME = "DB_USERNAME";
    public static final String DB_PASSWORD = "DB_PASSWORD";
    public static final String DB_SCHEMA = "DB_SCHEMA";

    public static final String NUM_SHARDS = "NUM_SHARDS";
    public static final String PREFIX = "PREFIX";


    private Config() {
        this.configFile = new File("res/config/config.conf");
        configs = new HashMap<>();

        try {
            setConfig();
        }
        catch (FileNotFoundException f) {
            // If config file is gone we can try using env vars
            configs = System.getenv();
            LOGGER.log(Level.WARNING, "Config file not found, defaulting to env vars");
        }
    }

    public static Config getInstance() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    private void setConfig() throws FileNotFoundException {
        Scanner scanner = new Scanner(configFile);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("***")){
                String key = line.substring(3, line.length()-3);
                configs.put(key, scanner.nextLine());
            }
        }
    }


    public String getConfig(String key) {
            return configs.get(key);
    }
}
