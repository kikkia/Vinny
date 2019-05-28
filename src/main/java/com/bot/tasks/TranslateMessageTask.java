package com.bot.tasks;

import com.bot.utils.HttpUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class TranslateMessageTask extends Thread {

    private MessageReceivedEvent event;

    public TranslateMessageTask(MessageReceivedEvent event) {
        this.event = event;
    }

    @Override
    public void run() {
        String preferredLanguage = "en"; // Will use guild settings at some point
        long startTime = System.currentTimeMillis();
        if (!event.getMessage().getContentRaw().isEmpty() && !event.getAuthor().isBot() && event.getMessage().getContentRaw().split(" ").length > 4) {
            String lang = HttpUtils.detectMessageLanguage(event.getMessage().getContentRaw());
            System.out.println("detected lang: " + lang + " | Message: " + event.getMessage().getContentRaw() + " | Time: " + (System.currentTimeMillis() - startTime));
        }
//        JSONObject response = HttpUtils.translateMessageToGivenLanguage(event.getMessage().getContentRaw(), preferredLanguage);
//        if (response == null) {
//            System.out.println("Failed to translate message"); // Log with error
//            return;
//        }
//
//        String fromLang = response.getString("fromLang");
//        if (!fromLang.equals(preferredLanguage)) {
//            //event.getTextChannel().sendMessage(response.getString("text")).queue();
//            System.out.println("Found non english message. " + fromLang + " found. Translated: " + response.getString("text"));
//            long totalTime = System.currentTimeMillis() - event.getMessage().getCreationTime().toInstant().toEpochMilli();
//            System.out.println("Total time: " + totalTime);
//        }
    }
}
