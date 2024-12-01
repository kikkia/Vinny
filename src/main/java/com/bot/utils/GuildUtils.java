package com.bot.utils;


import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;

import java.util.List;
import java.util.stream.Collectors;

public class GuildUtils {

    public static Role getHighestRole(Guild guild) {
        Role highest = null;
        for ( Role r : guild.getRoles()) {
            if (highest == null)
                highest = r;
            else if (highest.getPosition() < r.getPosition())
                highest = r;
        }
        return highest;
    }

    public static String convertListToPrefixesString(List<String> prefixes) {
        StringBuilder sb = new StringBuilder();
        for (String prefix : prefixes) {
            sb.append(prefix);
            sb.append(" ");
        }

        return sb.toString();
    }

    public static void sendWelcomeMessage(GuildJoinEvent guildJoinEvent) throws Exception {
        Logger logger = new Logger("Welcome-Message");

        List<TextChannel> textChannels = guildJoinEvent.getGuild().getTextChannels();

        // Look for a general channel and post in there.
        List<TextChannel> tmpList = textChannels.stream()
                .filter(c -> c.getName().equalsIgnoreCase("general"))
                .collect(Collectors.toList());

        if (tmpList.size() > 0 && tmpList.get(0).canTalk()) {
            tmpList.get(0).sendMessage(ConstantStrings.WELCOME_MESSAGE).queue(c -> logger.info("Sent guild welcome message to " + c.getChannel().getName()));
        } else {
            // No general channel or that we can post to, we will check for other channels we can post in
            List<TextChannel> postableChannels = textChannels.stream()
                    .filter(TextChannel::canTalk)
                    .filter(c -> !c.getName().toLowerCase().contains("announcements")) // Don't post to announcement channels.
                    .collect(Collectors.toList());
            if (postableChannels.size() < 1) {
                // We cant post to any so just give up
            } else if (postableChannels.size() == 1) {
                postableChannels.get(0).sendMessage(ConstantStrings.WELCOME_MESSAGE).queue(c -> logger.info("Sent guild welcome message to " + c.getChannel().getName()));
            } else {
                // We will get the highest channel.
                TextChannel highestChannel = null;
                for (TextChannel channel : postableChannels) {
                    // Higher number for position actually is lower on the list
                    if (highestChannel == null)
                        highestChannel = channel;
                    if (highestChannel.getPosition() > channel.getPosition())
                        highestChannel = channel;
                }
                highestChannel.sendMessage(ConstantStrings.WELCOME_MESSAGE).queue(c -> logger.info("Sent guild welcome message to " + c.getChannel().getName()));
            }
        }
    }

    public static Message getLastMessageFromChannel(TextChannel channel, boolean ignoreCommandMessage) {
        for (Message message : channel.getIterableHistory().cache(true)) {
            if (message.getIdLong() == channel.getLatestMessageIdLong())
                if (ignoreCommandMessage)
                    continue;
                else
                    return message;
            return message;
        }
        return null;
    }
}
