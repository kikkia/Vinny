package com.bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Config {

    private final File tokenFile;
    private final File configFile;
    private HashMap<String, String> tokens;
    private HashMap<String, String> configs;

    public Config() {
        this.tokenFile = new File("res/config/tokens.txt");
        this.configFile = new File("res/config/config.txt");
        tokens = new HashMap<>();
        configs = new HashMap<>();
        setTokens();
        setConfig();
    }

    private void setTokens() {
        try {
            Scanner scanner = new Scanner(tokenFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("***")){
                    String key = line.substring(3, line.length()-3);
                    tokens.put(key, scanner.nextLine());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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

    public String getToken(String key) {
        return tokens.get(key);
    }

    public String getConfig(String key) {
        return  configs.get(key);
    }
}
