package com.bot.utils;

import com.bot.exceptions.IntervalFormatException;
import com.bot.models.enums.RepeatMode;
import com.bot.voice.QueuedAudioTrack;
import com.jagrosh.jdautilities.command.CommandEvent;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FormattingUtils {

    private static final int MIN_INTERVAL = 300000;

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
        VinnyConfig config = VinnyConfig.Companion.instance();

        return switch (member.getOnlineStatus()) {
            case ONLINE -> config.getBotConfig().getOnlineEmoji();
            case IDLE -> config.getBotConfig().getIdleEmoji();
            case DO_NOT_DISTURB -> config.getBotConfig().getDndEmoji();
            default -> config.getBotConfig().getOfflineEmoji();
        };
    }

    public static MessageEmbed getAudioTrackEmbed(QueuedAudioTrack queuedAudioTrack, int volume, RepeatMode repeatMode, boolean autoplay) {
        EmbedBuilder builder = new EmbedBuilder();

        Track track = queuedAudioTrack.getTrack();

        builder.setTitle("Now Playing: ");
        builder.setDescription("[" + track.getInfo().getTitle() + "](" + track.getInfo().getUri() + ")");
        builder.addField("Duration", msToMinSec(track.getInfo().getLength()), false);
        builder.addField("Requested by", queuedAudioTrack.getRequesterName(), false);
        builder.addField("Autoplay", "" + autoplay, false);
        builder.setFooter("Volume: " + volume, null);
        builder.addField("Repeat Mode", repeatMode.getEzName(), false);

        builder.setColor(getColorForTrack(track.getInfo().getUri()));

        // If youtube, get the thumbnail
        if (track.getInfo().getUri().contains("www.youtube.com")) {
            String videoID = track.getInfo().getUri().split("=")[1];
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

    public static Color getColorForTrack(String uri) {
        Color toReturn = Color.BLACK; // Default
        if (uri.contains("youtube.com"))
            toReturn = Color.red;
        else if (uri.contains("soundcloud.com"))
            toReturn = Color.orange;
        else if (uri.contains("twitch.tv"))
            toReturn = new Color(100, 65, 165); // Twitch purple

        return toReturn;
    }

    public static List<String> getGamesPaginatedList(int pageSize, Map<String, List<Member>> gameMap) {
        List<String> list = new ArrayList<>();

        int maxPerGame = gameMap.size() >= 10 ? 3 : 5;
        for (Map.Entry<String, List<Member>> game : gameMap.entrySet()) {
            // TODO: Can we fit logic
            int remainingPageSpace = pageSize - (list.size() % pageSize);
            int requiredPageSpace = Math.min(game.getValue().size(), maxPerGame) + 2;

            if (remainingPageSpace < requiredPageSpace) {
                // If we don't have enough space then fill the page
                for (int i = remainingPageSpace; i > 0; i--) {
                    list.add(" ");
                }
            }
            list.add("**" + game.getKey() + "**");
            for (int i = 0; i < maxPerGame; i++) {
                if (i == game.getValue().size())
                    break;
                if (i == maxPerGame - 1 && game.getValue().size() - i > 1) {
                    list.add("and " + (game.getValue().size() - i) + " more.");
                    break;
                }
                list.add(getOnlineStatusEmoji(game.getValue().get(i)) + game.getValue().get(i).getEffectiveName());
            }
        }
        return list;
    }

    public static String cleanSayCommand(CommandEvent commandEvent) {
        StringBuilder sb = new StringBuilder();
        String[] words = commandEvent.getArgs().split(" ");

        Pattern inviteRegx = Pattern.compile("discord(?:app\\.com\\/invite|\\.gg)\\/([a-z0-9]{1,16})", Pattern.CASE_INSENSITIVE);
        Pattern userMentionRegx = Pattern.compile("\\<\\@([0-9]+)\\>");
        Pattern roleMentionRegx = Pattern.compile("\\<\\@\\&([0-9]+)\\>");

        for (String word: words) {

            // Escape everyone
            if (word.equalsIgnoreCase("@everyone")) {
                sb.append("(at)everyone ");
                continue;
            }

            // Escape here
            if (word.equalsIgnoreCase("@here")) {
                sb.append("(at)here ");
                continue;
            }

            // Escape all user mentions
            Matcher mentionMatcher = userMentionRegx.matcher(word);
            if (mentionMatcher.find()) {
                String userId = mentionMatcher.group(1);
                // If any users match the id
                List<User> mentionedUser = commandEvent.getMessage().getMentions().getUsers().stream()
                        .filter(user -> user.getId().equals(userId))
                        .collect(Collectors.toList());


                // Check if user was mentioned
                if (!mentionedUser.isEmpty()) {
                    sb.append(mentionedUser.get(0).getName()).append(" ");
                    continue;
                }
            }

            // Escape all role mentions
            Matcher roleMatcher = roleMentionRegx.matcher(word);
            if (roleMatcher.find()) {
                String roleId = roleMatcher.group(1);

                List<Role> mentionedRole = commandEvent.getMessage().getMentions().getRoles().stream()
                        .filter(role -> role.getId().equals(roleId))
                        .collect(Collectors.toList());

                if (!mentionedRole.isEmpty()) {
                   sb.append(mentionedRole.get(0).getName()).append(" ");
                   continue;
                }
            }

            // Get rid of invites
            Matcher matcher = inviteRegx.matcher(word);
            if (matcher.find()) {
                word = "`invite`";
            }

            sb.append(word).append(" ");
        }

        return sb.toString();
    }

    public static String clapify(String message) {
        StringBuilder sb = new StringBuilder();
        for (String s : message.split(" ")) {
            sb.append(" ").append(s).append(" :clap:");
        }
        return sb.toString();
    }

    // Time format to millis
    public static long getTimeForScheduledInput(String input) throws IntervalFormatException {
        try {
            String[] array = input.split(":");
            // if there is too many or not enough, then throw an error
            if (array.length > 5) {
                throw new IntervalFormatException("Too many args given. Please use the format `ww:dd:hh:mm:ss`");
            } else if (array.length < 2) {
                throw new IntervalFormatException("Invalid interval. Please use the format `ww:dd:hh:mm:ss");
            } else {
                // Array to use to convert indexes to millis
                int[] convertUnits = new int[] {1000, 60000, 3600000, 86400000, 604800000};
                int index = 0;
                long interval = 0;

                // Add millis for each index
                for (int i = array.length - 1; i >= 0; i--) {
                    int val = Integer.parseInt(array[i]);
                    interval += (long) val * convertUnits[index];
                    index++;
                }

                // Min time is one minute
                if (interval < MIN_INTERVAL) {
                    throw new IntervalFormatException("Interval cannot be smaller than 5 minutes");
                }

                return interval;
            }
        } catch (Exception e) {
            throw new IntervalFormatException(e.getMessage());
        }
    }

    /**
     * Convert a millisecond duration to a string format
     *
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis) {
        if(millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        String sb = days +
                " Days " +
                hours +
                " Hours " +
                minutes +
                " Minutes " +
                seconds +
                " Seconds";

        return(sb);
    }

    public static String getDateForMillis(long millis) {
        Date date = new Date(millis);
        DateFormat df = new SimpleDateFormat("dd:MM:yy:HH:mm:ss");
        return df.format(date);
    }
}
