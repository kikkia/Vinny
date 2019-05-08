package com.bot.utils;

import com.bot.voice.QueuedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
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

    public static MessageEmbed getAudioTrackEmbed(QueuedAudioTrack queuedAudioTrack) {
        EmbedBuilder builder = new EmbedBuilder();

        AudioTrack track = queuedAudioTrack.getTrack();

        builder.setTitle("Now Playing: ");
        builder.setDescription(track.getInfo().title);
        builder.addField("Duration", msToMinSec(track.getInfo().length), false);
        builder.addField("Requested by", queuedAudioTrack.getRequesterName(), false);
        builder.setFooter(track.getInfo().uri, null);

        // If youtube, get the thumbnail
        if (track.getInfo().uri.contains("www.youtube.com")) {
            String videoID = track.getInfo().uri.split("=")[1];
            builder.setThumbnail("https://img.youtube.com/vi/" + videoID + "/0.jpg");
        }

        return builder.build();
    }

    //Helper method for song that takes length in Milliseconds and outputs it in a more readable HH:MM:SS format
    public static String msToMinSec(long length) {
        int totSeconds = (int)length/1000;
        String seconds = "";
        String minutes = "";
        String hours = "";
        if (totSeconds%60 < 10)
            seconds = "0" + totSeconds%60;
        else
            seconds += totSeconds%60;
        if (totSeconds/60 < 10)
            minutes = "0" + totSeconds/60;
        else if (totSeconds/60 > 59)
            minutes += (totSeconds/60)%60;
        else
            minutes += totSeconds/60;
        if (totSeconds/3600 < 10)
            hours = "0" + (totSeconds/60)/60;
        else
            hours += (totSeconds/60)/60;

        if ("00".equals(hours))
            return minutes + ":" + seconds;
        else {
            if (minutes.length() == 1)
                minutes = "0" + minutes;
            return hours + ":" + minutes + ":" + seconds;
        }
    }

}
