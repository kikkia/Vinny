package com.bot.utils;

import com.sedmelluq.discord.lavaplayer.tools.Ipv4Block;
import com.sedmelluq.discord.lavaplayer.tools.http.AbstractRoutePlanner;
import com.sedmelluq.discord.lavaplayer.tools.http.RotatingIpRoutePlanner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class LavaPlayerUtils {
    private static Logger logger = new Logger(LavaPlayerUtils.class.getName());

    public static AbstractRoutePlanner getIPRoutePlanner() {
        Config config = Config.getInstance();

        // If not enabled then return null
        if (!Boolean.parseBoolean(config.getConfig(Config.ENABLE_LOGGING_CHANNELS))) {
            return null;
        }

        Ipv4Block block = new Ipv4Block(config.getConfig(Config.IPV4_IP_BLOCK));
        Map<InetAddress, String> excluded = new HashMap<>();

        // Make map of excluded addresses
        try {
            for (String s : config.getConfig(Config.EXCLUDED_ADDRESSES).split(",")) {
                excluded.put(InetAddress.getByName(s), s);
            }
        } catch (UnknownHostException e) {
            logger.severe("Failed to make IP exclude map", e);
            return null;
        }

        Predicate<InetAddress> filter = (inetAddress -> !excluded.containsKey(inetAddress));

        return new RotatingIpRoutePlanner(block, filter, true);
    }
}
