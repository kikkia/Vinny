package com.bot.commands.nsfw;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.bot.caching.R34Cache;
import com.bot.commands.NSFWCommand;
import com.bot.exceptions.ScheduledCommandFailedException;
import com.bot.utils.ScheduledCommandUtils;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule34Command extends NSFWCommand {
    private Random random;
    private R34Cache cache;

    public Rule34Command() {
        this.name = "r34";
        this.aliases = new String[]{"rule34"};
        this.arguments = "<tags to search for>";
        this.cooldown = 1;
        this.help = "Gets rule 34 for the given tags";

        this.random = new Random(System.currentTimeMillis());
        this.cache = R34Cache.getInstance();
    }

    @Override
    protected void executeCommand(CommandEvent commandEvent) {
        // Get the tags
        String r34url = "http://rule34.xxx/index.php?page=dapi&s=post&q=index&limit=250&tags=" + commandEvent.getArgs();
        String booruUrl = "https://yande.re/post.xml?tags=" + commandEvent.getArgs();
        List<String> imageUrls = cache.get(commandEvent.getArgs());
        String selected;
        try {
            if (imageUrls == null) {
                imageUrls = new ArrayList<>();
                imageUrls.addAll(getImageURLFromSearch(r34url));
                imageUrls.addAll(getImageURLFromSearch(booruUrl));
                cache.put(commandEvent.getArgs(), imageUrls);
            }
            selected = imageUrls.get(random.nextInt(imageUrls.size()));
        } catch (IllegalArgumentException e) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " I couldn't find any results for that search.");
            return;
        } catch (Exception e) {
            if (imageUrls == null || imageUrls.isEmpty()) {
                logger.severe("Something went wrong getting r34 post: ", e);
                commandEvent.reply(commandEvent.getClient().getError() + " Something went wrong getting the image, please try again.");
                metricsManager.markCommandFailed(this, commandEvent.getAuthor(), commandEvent.getGuild());
                return;
            } else {
                logger.warning("Failed to get some r34 posts, but some exist... Attempting to send them", e);
                cache.put(commandEvent.getArgs(), imageUrls);
                selected = imageUrls.get(random.nextInt(imageUrls.size()));
            }
        }
        if (ScheduledCommandUtils.isScheduled(commandEvent)) {
            WebhookClient client = null;
            try {
                client = ScheduledCommandUtils.getWebhookForChannel(commandEvent);
                client.send(buildWebhookMessage(selected, commandEvent));
            } catch (ScheduledCommandFailedException e) {
                logger.warning("Failed to get webhook r34", e);
                commandEvent.replyWarning(e.getMessage());
            }
        } else {
            commandEvent.reply(selected);
        }
    }

    private WebhookMessage buildWebhookMessage(String selected, CommandEvent commandEvent) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setAvatarUrl(commandEvent.getSelfUser().getAvatarUrl());
        builder.setUsername(commandEvent.getSelfMember().getEffectiveName());
        builder.setContent(selected);
        return builder.build();
    }

    private List<String> getImageURLFromSearch(String url) throws Exception{
        HttpGet get = new HttpGet(url);
        int timeout = 5;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {

            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };

            String responseBody = client.execute(get, responseHandler);
            client.close();

            // Regex the returned xml and get all links
            Pattern expression = Pattern.compile("(file_url)=[\"']?((?:.(?![\"']?\\s+(?:\\S+)=|[>\"']))+.)[\"']?");
            Matcher matcher = expression.matcher(responseBody);
            ArrayList<String> possibleLinks = new ArrayList<>();

            while (matcher.find()) {
                // Add the second group of regex
                possibleLinks.add(matcher.group(2));
            }

            return possibleLinks;
        }
    }
}
