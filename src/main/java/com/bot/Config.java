package com.bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Config {

    private final File tokenFile;
    private HashMap<String, String> tokens;

    public Config() {
        this.tokenFile = new File("res/config/tokens.txt");
        tokens = new HashMap<>();
        setTokens();
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

    public String getToken(String key) {
        return tokens.get(key);
    }
}
