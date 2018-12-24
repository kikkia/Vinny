package com.bot.Utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattingUtils {

    public static ArrayList<String> splitTextIntoChunksByWords(String input) {
        ArrayList<String> stringList = new ArrayList<>();

        String regex = "\\s*(.{1800}[^.]*\\.|.+$)";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                stringList.add(matcher.group(i));
            }
        }

        return stringList;
    }
}
