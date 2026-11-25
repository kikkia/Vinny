package com.bot.commands.traditional.nsfw;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.bot.commands.traditional.NSFWCommand;
import com.bot.exceptions.ScheduledCommandFailedException;
import com.bot.utils.HttpUtils;
import com.bot.utils.ScheduledCommandUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;

public class R4cCommand extends NSFWCommand {

    public R4cCommand() {
        this.name = "4chan";
        this.arguments = "<4chan board>";
        this.help = "Gets a random thread from a given 4chan board";
        this.aliases = new String[]{"random4chan", "r4chan", "random4c", "r4c"};
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "4chan")
    protected void executeCommand(CommandEvent commandEvent) {
        JSONObject thread = HttpUtils.getRandom4chanThreadFromBoard(commandEvent.getArgs());

        if (thread == null) {
            commandEvent.replyWarning("Something went wrong, make sure you have a correct board name");
            return;
        }

        String imageUrl = extractImageUrl(commandEvent.getArgs(), thread);
        String ext = thread.optString("ext", "");

        String title = thread.optString("name", "Anonymous");
        if (!thread.isNull("sub") && !thread.optString("sub", "").isEmpty()) {
            title = thread.getString("sub");
        }

        String body;
        if (thread.has("com") && !thread.isNull("com")) {
            body = thread.getString("com");
            body = StringEscapeUtils.unescapeHtml4(body);
            body = body.replaceAll("<br>", "\n");
            body = body.replaceAll("<[^>]*>", "");
        } else {
            body = "No comment found";
        }

        if (body.length() > 250) {
            body = body.substring(0, 250) + "...";
        }
        if (body.isEmpty()) {
            body = "No comment found";
        }

        if (ScheduledCommandUtils.isScheduled(commandEvent)) {
            try {
                WebhookClient client = ScheduledCommandUtils.getWebhookForChannel(commandEvent);
                client.send(buildWebhookMessage(commandEvent, imageUrl, title, thread, body));
            } catch (ScheduledCommandFailedException e) {
                logger.warning("r4c failed to get webhook", e);
                commandEvent.replyWarning(e.getMessage());
            }

        } else {
            EmbedBuilder builder = new EmbedBuilder();
            if (imageUrl != null) {
                builder.setImage(imageUrl);
            }
            builder.setAuthor(title);
            builder.setDescription("Replies: " + thread.optInt("replies", 0) + " Images: " + thread.optInt("images", 0));
            builder.setTitle(body);
            builder.addField("link", "[Thread](http://boards.4channel.org/" + commandEvent.getArgs() + "/thread/" + thread.optInt("no", 0) + ")", false);
            commandEvent.reply(builder.build());

            if (imageUrl != null && ".webm".equals(ext)) {
                commandEvent.reply(imageUrl);
            }
        }
    }

    public static String extractImageUrl(String board, JSONObject thread) {
        if (thread == null || !thread.has("tim") || thread.isNull("tim")
                || !thread.has("ext") || thread.isNull("ext")) {
            return null;
        }
        long imageNum = thread.getLong("tim");
        String ext = thread.getString("ext");
        if (ext.isEmpty()) {
            return null;
        }
        return "http://i.4cdn.org/" + board + "/" + imageNum + ext;
    }

    private WebhookMessage buildWebhookMessage(CommandEvent commandEvent, String imageUrl, String title, JSONObject thread, String body) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        WebhookEmbedBuilder embedBuilder = new WebhookEmbedBuilder();
        if (imageUrl != null) {
            embedBuilder.setImageUrl(imageUrl);
        }
        embedBuilder.setTitle(new WebhookEmbed.EmbedTitle("/" + commandEvent.getArgs() + "/",
                "http://boards.4channel.org/" + commandEvent.getArgs() + "/thread/" + thread.optInt("no", 0)));
        embedBuilder.setDescription(body);
        embedBuilder.addField(new WebhookEmbed.EmbedField(true, "Replies", thread.optInt("replies", 0) + ""));
        embedBuilder.addField(new WebhookEmbed.EmbedField(true, "Images", thread.optInt("images", 0) + ""));
        builder.addEmbeds(embedBuilder.build());
        return builder.build();
    }
}
