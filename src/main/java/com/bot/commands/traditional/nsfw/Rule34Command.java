package com.bot.commands.traditional.nsfw;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.bot.caching.R34Cache;
import com.bot.commands.traditional.NSFWCommand;
import com.bot.exceptions.ScheduledCommandFailedException;
import com.bot.models.enums.R34Provider;
import com.bot.utils.ScheduledCommandUtils;
import com.bot.utils.TheGreatCCPFilter;
import com.bot.utils.VinnyConfig;
import com.jagrosh.jdautilities.command.CommandEvent;
import datadog.trace.api.Trace;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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
import java.util.stream.Collectors;

public class Rule34Command extends NSFWCommand {
    private final Random random;
    private final R34Cache cache;
    private String xxKey;
    private String xxUser;

    public Rule34Command() {
        this.name = "r34";
        this.aliases = new String[]{"rule34"};
        this.arguments = "<tags to search for>";
        this.help = "Gets rule 34 for the given tags";

        this.random = new Random(System.currentTimeMillis());
        this.cache = R34Cache.getInstance();
        this.xxKey = VinnyConfig.Companion.instance().getThirdPartyConfig().getR34ApiKey();
        this.xxUser = VinnyConfig.Companion.instance().getThirdPartyConfig().getR34UserId();
    }

    @Override
    @Trace(operationName = "executeCommand", resourceName = "rule34")
    protected void executeCommand(CommandEvent commandEvent) {
        // This stuff is against discord ToS
        if (TheGreatCCPFilter.Companion.containsNoNoTags(commandEvent.getArgs())) {
            commandEvent.replyWarning("The search terms contain tags that are banned on discord, so I can't post them here.");
            return;
        }

        String args = commandEvent.getArgs().replaceAll(" ", "+");
        String r34url = "http://api.rule34.xxx/index.php?page=dapi&s=post&q=index&limit=200&api_key=" + xxKey + "&user_id=" + xxUser +  "&tags=" + args;
        String booruUrl = "https://yande.re/post.xml?limit=200&tags=" + args;
        String pahealUrl = "https://rule34.paheal.net/rss/images/" + commandEvent.getArgs().replaceAll(" ", "%20") + "/1";
        List<String> imageUrls = cache.get(args);
        String selected;
        try {
            if (imageUrls == null) {
                imageUrls = new ArrayList<>();
                imageUrls.addAll(getImageURLFromSearch(r34url, R34Provider.XXX));
                imageUrls.addAll(getImageURLFromSearch(booruUrl, R34Provider.YANDERE));
                imageUrls.addAll(getImageURLFromSearch(pahealUrl, R34Provider.PAHEAL));
                cache.put(args, imageUrls);
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
            WebhookClient client;
            try {
                client = ScheduledCommandUtils.getWebhookForChannel(commandEvent);
                client.send(buildWebhookMessage(selected, commandEvent));
            } catch (ScheduledCommandFailedException e) {
                logger.warning("Failed to get webhook r34", e);
                commandEvent.replyWarning(e.getMessage());
            }
        } else {
            String refreshButtonId = "refresh-r34-" + args;
            Button refresh = Button.primary(refreshButtonId, Emoji.fromUnicode("\uD83D\uDD04"));
            commandEvent.getChannel().sendMessage(selected).addActionRow(refresh).queue();
        }
    }

    private WebhookMessage buildWebhookMessage(String selected, CommandEvent commandEvent) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setAvatarUrl(commandEvent.getSelfUser().getAvatarUrl());
        builder.setUsername(commandEvent.getSelfMember().getEffectiveName());
        builder.setContent(selected);
        return builder.build();
    }

    private List<String> getImageURLFromSearch(String url, R34Provider provider) throws Exception{
        HttpGet get = new HttpGet(url);
        metricsManager.markR34Request(provider);
        int timeout = 4;
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

            // Regex the returned xml and get all links different regex based on source
            Pattern expression = url.contains("paheal.net") ?
                    Pattern.compile("(<media:content url=)\"([\\s\\S]*?)\"\\/>")
                    : Pattern.compile("(sample_url)=[\"']?((?:.(?![\"']?\\s+(?:\\S+)=|[>\"']))+.)[\"']?");
            Matcher matcher = expression.matcher(responseBody);
            List<String> possibleLinks = new ArrayList<>();

            while (matcher.find()) {
                // Look for mp4 and change subdomain to allow for embedding
                String link = matcher.group(2);
                if (link.contains(".mp4") && link.contains("api-cdn.rule34.xxx")) {
                    link = link.replace("api-cdn.rule", "api-cdn-mp4.rule");
                }
                possibleLinks.add(link);
            }

            // Some URLs contain post tags, scan URLs for things banned on discord
            possibleLinks = possibleLinks.stream().filter(
                    it -> !TheGreatCCPFilter.Companion.containsNoNoTags(it))
                    .collect(Collectors.toList());
            metricsManager.markR34Response(provider, true);
            metricsManager.markR34ResponseSize(provider, possibleLinks.size());
            return possibleLinks;
        } catch (Exception e) {
            metricsManager.markR34Response(provider, false);
            logger.warning("Failed to fetch r34 posts for source: " + url, e);
            return new ArrayList<>();
        }
    }
}
