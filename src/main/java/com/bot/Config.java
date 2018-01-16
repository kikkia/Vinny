package com.bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Config {

    private static Config config = null;
    private final File configFile;
    private HashMap<String, String> configs;

    public static final String DISCORD_TOKEN = "Discord";
    public static final String REDDIT_TOKEN = "Reddit";
    public static final String OWNER_ID = "Owner";
    public static final String DISCORD_BOT_ID = "Bot_Id";

    public static final String USE_DB = "Use_DB";
    public static final String DB_URI = "DB_URI";
    public static final String DB_USERNAME = "DB_Username";
    public static final String DB_PASSWORD = "DB_Password";
    public static final String DB_SCHEMA = "DB_Schema";

    public static final String NUM_SHARDS = "Num_Shards";
    public static final String PREFIX = "Prefix";


    private Config() {
        this.configFile = new File("res/config/config.conf");
        configs = new HashMap<>();
        setConfig();
    }

    public static Config getInstance() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    private void setConfig() {
        try {
            Scanner scanner = new Scanner(configFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("***")){
                    String key = line.substring(3, line.length()-3);
                    configs.put(key, scanner.nextLine());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public String getConfig(String key) {
        return  configs.get(key);
    }
}
