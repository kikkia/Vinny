package com.bot.utils;

import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingIpRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv4Block;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class LavaPlayerUtils {
    private static Logger logger = new Logger(LavaPlayerUtils.class.getName());

    public static AbstractRoutePlanner getIPRoutePlanner() {
        Config config = Config.getInstance();

        // If not enabled then return null
        if (!Boolean.parseBoolean(config.getConfig(Config.ENABLE_YT_IP_ROUTING))) {
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

        // Filter addresses in the blacklist out
        Predicate<InetAddress> filter = (inetAddress -> !excluded.containsKey(inetAddress));
        logger.info("Creating routeplanner with " + block.getSize() + " ips");

        List<IpBlock> blocks = new ArrayList<>();
        blocks.add(block);
        return new RotatingIpRoutePlanner(blocks, filter, true);
    }
}
