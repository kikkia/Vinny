package com.bot.utils;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattingUtils {

    public static ArrayList<String> splitTextIntoChunksByWords(String input, int chunkLength) {
        ArrayList<String> stringList = new ArrayList<>();

        String regex = "\\s*(.{" + chunkLength + "}[^.]*\\.|.+$)";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                stringList.add(matcher.group(i));
            }
        }

        return stringList;
    }

    public static String formattedRolesList(Member member) {
        StringBuilder rolesBuilder = new StringBuilder();
        for (Role r: member.getRoles()) {
            rolesBuilder.append(r.getAsMention());
        }
        return rolesBuilder.toString();
    }

    public static String formatOffsetDateTimeToDay(OffsetDateTime offsetDateTime) {
        // Return the yyyy-mm-dd only from the string
        return offsetDateTime.toString().split("T")[0];
    }

    public static String getOnlineStatusEmoji(Member member) {
        Config config = Config.getInstance();

        switch (member.getOnlineStatus()) {
            case ONLINE:
                return config.getConfig(Config.ONLINE_EMOJI);
            case IDLE:
                return config.getConfig(Config.IDLE_EMOJI);
            case DO_NOT_DISTURB:
                return config.getConfig(Config.DND_EMOJI);
            default:
                return config.getConfig(Config.OFFLINE_EMOJI);
        }
    }
}
