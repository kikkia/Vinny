package com.bot.utils;

import com.bot.exceptions.BlueskyException;
import com.bot.exceptions.InvalidInputException;
import com.bot.exceptions.NoSuchResourceException;
import com.bot.models.MarkovModel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HttpUtils {
    private static final Logger logger = new Logger(HttpUtils.class.getName());
    private static final Random random = new Random(System.currentTimeMillis());

    private static final VinnyConfig config = VinnyConfig.Companion.instance();

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/99.0.3538.77 Safari/537.36";
    private static final Pattern YT_CHANNEL_ID_PATTERN = Pattern.compile("^UC[A-Za-z0-9_-]{22}$");
    private static final Pattern YT_CHANNEL_URL_PATTERN = Pattern.compile("https?://(?:www\\.|m\\.)?youtube\\.com/channel/(UC[A-Za-z0-9_-]{22})");
    private static final Pattern YT_HANDLE_PATTERN = Pattern.compile("^@[A-Za-z0-9_.-]+$");
    private static final Pattern[] YT_HTML_ID_PATTERNS = new Pattern[] {
            Pattern.compile("<meta\\s+itemprop=\"identifier\"\\s+content=\"(UC[A-Za-z0-9_-]{22})\""),
            Pattern.compile("<link\\s+rel=\"canonical\"\\s+href=\"https?://(?:www\\.)?youtube\\.com/channel/(UC[A-Za-z0-9_-]{22})\""),
            Pattern.compile("\"(?:externalId|channelId|browseId)\"\\s*:\\s*\"(UC[A-Za-z0-9_-]{22})\"")
    };
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(HttpUtils.class);

    //  |****************************************************|
    //  |                       4chan                        |
    //  |****************************************************|

    public static JSONObject getRandom4chanThreadFromBoard(String board) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String boardUrl = "https://a.4cdn.org/" + board + "/threads.json";
            HttpGet get = new HttpGet(boardUrl);

            HttpResponse response = client.execute(get);
            // Convert response into a json array
            String json = IOUtils.toString(response.getEntity().getContent());
            JSONArray array = new JSONArray(json);
            // Choose a random thread in the array
            JSONObject page = array.getJSONObject(random.nextInt(array.length()));
            array = page.getJSONArray("threads");
            JSONObject thread = array.getJSONObject(random.nextInt(array.length()));

            return getInfoForThread(thread.getLong("no"), board);
        } catch (Exception e) {
            return null;
        }
    }

    public static JSONObject getInfoForThread(long id, String board) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String threadUrl = "http://a.4cdn.org/" + board + "/thread/" + id + ".json";
            HttpGet get = new HttpGet(threadUrl);
            HttpResponse response = client.execute(get);
            // Convert response into a json array
            String json = IOUtils.toString(response.getEntity().getContent());
            JSONObject thread = new JSONObject(json);
            JSONArray array = thread.getJSONArray("posts");

            return array.getJSONObject(0);
        } catch (Exception e) {
            return null;
        }
    }

    // TODO: Port to use a Webhook client like used for some scheduled commands
    public static void sendCommentHook(Webhook webhook, MarkovModel model, Member member, MessageChannel channel) throws Exception {
        JSONObject toSend = getMessage(model, member, channel);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(webhook.getUrl());
            post.addHeader("Content-type", "application/json");
            post.setEntity(new StringEntity(toSend.toString()));

            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() >= 400) {
                try (MDC.MDCCloseable closeable = MDC.putCloseable("error_message",
                        IOUtils.toString(response.getEntity().getContent()))) {
                    logger.warning("Posting to discord webhook failed with code " +
                            response.getStatusLine().getStatusCode());
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to send webhook for comment", e);
            throw new RuntimeException("Failed to send webhook to channel.");
        }
    }

    @NotNull
    private static JSONObject getMessage(MarkovModel model, Member member, MessageChannel channel) {
        String message = model.getPhrase();
        JSONObject toSend = new JSONObject();

        message = message.replaceAll("@", "(at)");

        if (member != null) {
            toSend.put("username", member.getEffectiveName());
            toSend.put("avatar_url", member.getUser().getAvatarUrl());
        } else {
            toSend.put("username", channel.getName());
            String iconUrl = channel.getJDA().getSelfUser().getAvatarUrl();
            if (channel instanceof GuildChannel) {
               iconUrl = ((GuildChannel) channel).getGuild().getIconUrl();
            }
            toSend.put("avatar_url", iconUrl);
        }
        toSend.put("content", message);
        return toSend;
    }

    public static String getYoutubeIdForChannelUrl(String input) throws IOException, NoSuchResourceException, InvalidInputException {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidInputException("Not youtube url");
        }

        String trimmed = input.trim();
        if (YT_CHANNEL_ID_PATTERN.matcher(trimmed).matches()) {
            return trimmed;
        }

        String normalizedUrl = normalizeYoutubeUrl(trimmed);
        Matcher directMatcher = YT_CHANNEL_URL_PATTERN.matcher(normalizedUrl);
        if (directMatcher.find()) {
            return directMatcher.group(1);
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(normalizedUrl);
            httpget.addHeader("User-Agent", USER_AGENT);
            httpget.addHeader("Accept-Language", "en-US,en;q=0.9");
            HttpResponse response = client.execute(httpget);
            int status = response.getStatusLine().getStatusCode();
            if (status == 404) {
                throw new NoSuchResourceException("Could not find that YT channel");
            }
            if (status >= 400 || response.getEntity() == null) {
                throw new IOException("Unexpected HTTP status " + status + " fetching YouTube channel");
            }
            String html = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            String channelId = extractYoutubeChannelIdFromHtml(html);
            if (channelId == null) {
                throw new NoSuchResourceException("Could not find that YT channel");
            }
            return channelId;
        }
    }

    public static String normalizeYoutubeUrl(String input) throws InvalidInputException {
        String trimmed = input.trim();
        if (YT_HANDLE_PATTERN.matcher(trimmed).matches()) {
            return "https://www.youtube.com/" + trimmed;
        }
        String withScheme = (trimmed.startsWith("http://") || trimmed.startsWith("https://"))
                ? trimmed
                : "https://" + trimmed;
        if (!withScheme.matches("^https?://(?:www\\.|m\\.)?youtube\\.com/.+$")) {
            throw new InvalidInputException("Not youtube url");
        }
        return withScheme;
    }

    public static String extractYoutubeChannelIdFromHtml(String html) {
        if (html == null || html.isEmpty()) {
            return null;
        }
        for (Pattern pattern : YT_HTML_ID_PATTERNS) {
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public static String getHashforImage(String link) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://hash.kikkia.dev/api/link");
            JSONObject payload = new JSONObject().put("url", link);
            StringEntity entity = new StringEntity(payload.toString());
            post.setEntity(entity);
            post.setHeader("Content-Type", "application/json");
            HttpResponse response = client.execute(post);
            return IOUtils.toString(response.getEntity().getContent());

        } catch (Exception e) {
            logger.severe("Failed to get neuralhash response", e);
            // In this case just return blank to allow image through
            return "";
        }
    }

    public static String getBlueSkyRSS(String username) {
        String url = "https://bluesky.kikkia.dev/user/" + username;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);

            if (response.getStatusLine().getStatusCode() == 404) {
                throw new BlueskyException("Profile not found, please double check the handle and try again.");
            } else if (response.getStatusLine().getStatusCode() > 500) {
                throw new BlueskyException("I was not able to reach bluesky. Please " +
                        "wait a bit and try again.");
            }

            return new JSONObject(IOUtils.toString(response.getEntity().getContent())).getString("rssUrl");
        } catch (Exception e) {
            logger.severe("Failed to get BlueSky RSS URL: " + e.getMessage(), e);
            throw new BlueskyException("Failed to fetch BlueSky profile, Please double check that the" +
                    " handle is correct, or report this on the support server.");
        }
    }
}