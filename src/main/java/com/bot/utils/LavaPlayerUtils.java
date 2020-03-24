package com.bot.utils;

import com.bot.voice.MyRotatingIpPlanner;
import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.planner.BalancingIpRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv4Block;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.bot.utils.Config.IPV6_IP_BLOCK;

public class LavaPlayerUtils {
    private static Logger logger = new Logger(LavaPlayerUtils.class.getName());

    public static AbstractRoutePlanner getIPRoutePlanner() {
        Config config = Config.getInstance();

        if (Boolean.parseBoolean(config.getConfig(Config.ENABLE_YT_IP_ROUTING_V6))) {
           return getIPV6RoutePlanner();
        }

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
        logger.info("Creating routeplanner with " + (block.getSize().intValue() - excluded.size()) + " ips");

        List<IpBlock> blocks = new ArrayList<>();
        blocks.add(block);
        return new MyRotatingIpPlanner(blocks, filter, true);
    }

    private static AbstractRoutePlanner getIPV6RoutePlanner() {
        Config config = Config.getInstance();
        Ipv6Block ipv6Block = new Ipv6Block(config.getConfig(IPV6_IP_BLOCK));

        Map<InetAddress, String> excluded = new HashMap<>();

        // Filter addresses in the blacklist out
        Predicate<InetAddress> filter = (inetAddress -> !excluded.containsKey(inetAddress));

        List<IpBlock> blocks = new ArrayList<>();
        blocks.add(ipv6Block);
        return new BalancingIpRoutePlanner(blocks, filter, true);
    }
}
