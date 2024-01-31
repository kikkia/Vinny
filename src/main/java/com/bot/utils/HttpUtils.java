package com.bot.utils;

import com.bot.exceptions.InvalidInputException;
import com.bot.exceptions.NoSuchResourceException;
import com.bot.models.MarkovModel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;


public class HttpUtils {
    private static final Logger logger = new Logger(HttpUtils.class.getName());
    private static final Random random = new Random(System.currentTimeMillis());

    private static final VinnyConfig config = VinnyConfig.Companion.instance();

    private static final String P90_BASE_URL = "https://p90.zone/";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/99.0.3538.77 Safari/537.36";
    // Unauthed cookies to tag on request to allow site to parse request as if we are an unauthed browser.
    // Exposing these tokens causes no risk as they are anonymous
    private static final String YT_COMMENT_PICK_S = "PHPSESSID=em0nb7ven1rdgndgmg7oopr7ft";

     //  |****************************************************|
     //  |                       4chan                        |
     //  |****************************************************|

    public static JSONObject getRandom4chanThreadFromBoard(String board) {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
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
        try(CloseableHttpClient client = HttpClients.createDefault()) {
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

    public static String getRandomP90Post(boolean canNSFW, String search) throws Exception {
        String url = search.isEmpty() ? P90_BASE_URL + "api/random" : P90_BASE_URL + "api/search/" + search.split(" ")[0];
        // Search is always nsfw, random can be locked down.
        url = (canNSFW || !search.isEmpty()) ? url : url + "?nsfw=0";
        String token = "key " + Objects.requireNonNull(config.getThirdPartyConfig()).getP90Token();
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            get.addHeader("Authorization", token);
            HttpResponse response = client.execute(get);
            String json = IOUtils.toString(response.getEntity().getContent());

            String name = "";
            // If random post then get name
            if (search.isEmpty()) {
                JSONObject post = new JSONObject(json);
                name = post.getString("name");
            } else { // On search get random entry
                // On search we get a json array back
                JSONArray array = new JSONArray(json);
                JSONObject selectedObject = array.getJSONObject(random.nextInt(array.length()));
                name = selectedObject.getString("name");
            }

            return P90_BASE_URL + name;
        }
    }

    // TODO: Port to use a Webhook client like used for some scheduled commands
    public static void sendCommentHook(Webhook webhook, MarkovModel model, Member member, TextChannel channel) throws Exception {
        String message = model.getPhrase();
        JSONObject toSend = new JSONObject();

        message = message.replaceAll("@", "(at)");

        if (member != null) {
            toSend.put("username", member.getEffectiveName());
            toSend.put("avatar_url", member.getUser().getAvatarUrl());
        } else {
            toSend.put("username", channel.getName());
            toSend.put("avatar_url", channel.getGuild().getIconUrl());
        }
        toSend.put("content", message);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(webhook.getUrl());
            post.addHeader("Content-type", "application/json");
            post.setEntity(new StringEntity(toSend.toString()));

            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() >= 400) {
                try(MDC.MDCCloseable closeable = MDC.putCloseable("error_message",
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

    public static byte[] getUrlAsByteArray(String uri, String refererUrl) {
        try (CloseableHttpClient client = HttpClients.createDefault()){
            HttpGet httpget = new HttpGet(uri);
            httpget.addHeader("referer", refererUrl);
            HttpResponse response = client.execute(httpget);
            HttpEntity entity = response.getEntity();
            try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                entity.writeTo(baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            logger.severe("Failed to get url as byteArray", e);
            return null;
        }
    }

    public static String getTwitchIdForUsername(String username) throws IOException, NoSuchResourceException {
        String uri = "https://api.twitch.tv/kraken/users?login=" + username;
        try (CloseableHttpClient client = HttpClients.createDefault()){
            HttpGet httpget = new HttpGet(uri);
            httpget.addHeader("Client-ID", Objects.requireNonNull(config.getThirdPartyConfig()).getTwitchClientId());
            httpget.addHeader("Accept", "application/vnd.twitchtv.v5+json");
            HttpResponse response = client.execute(httpget);
            JSONObject jsonResponse = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
            if (jsonResponse.getInt("_total") == 0) {
                throw new NoSuchResourceException("User not found");
            }
            return jsonResponse.getJSONArray("users").getJSONObject(0).getString("_id");
        }
    }

    public static String getYoutubeIdForChannelUrl(String url) throws IOException, NoSuchResourceException, InvalidInputException {
        boolean lookup = url.contains("https://www.youtube.com/c/");
        String token = getYTChannelIdToken();
        String uri = lookup ? buildYoutubeSearchUrl(url, token) : buildYoutubeLookupUri(url, token);
        try (CloseableHttpClient client = HttpClients.createDefault()){
            HttpGet httpget = new HttpGet(uri);
            httpget.addHeader("referer", "https://commentpicker.com/youtube-channel-id.php");
            httpget.addHeader("User-Agent", USER_AGENT);
            httpget.addHeader("Cookie", YT_COMMENT_PICK_S);
            HttpResponse response = client.execute(httpget);
            try {
                JSONObject jsonResponse = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
                return lookup ? jsonResponse.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("channelId")
                        : jsonResponse.getJSONArray("items").getJSONObject(0).getString("id");
            } catch (JSONException e) {
                logger.severe("Failed to parse yt channel id", e);
                throw new NoSuchResourceException("Could not find that YT channel");
            }
        }
    }

    private static String getYTChannelIdToken() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet("https://commentpicker.com/actions/token.php");
            httpget.addHeader("referer", "https://commentpicker.com/youtube-channel-id.php");
            httpget.addHeader("User-Agent", USER_AGENT);
            httpget.addHeader("Cookie", YT_COMMENT_PICK_S);
            HttpResponse response = client.execute(httpget);
            return IOUtils.toString(response.getEntity().getContent());
        }
    }

    // With some channels we will need to search rather than direct lookup
    private static String buildYoutubeSearchUrl(String channel, String token) throws InvalidInputException {
        if (!channel.contains("https://www.youtube.com/")) {
            throw new InvalidInputException("Not youtube url");
        }
        return "https://commentpicker.com/actions/youtube-channel-id.php?url=https%3A%2F%2Fwww.googleapis.com%2Fyoutube" +
                "%2Fv3%2Fsearch%3Fpart%3Did%2Csnippet%26type%3Dchannel%26q%3D" +
                channel.split("/")[channel.split("/").length-1] + "&token=" + token;
    }

    private static String buildYoutubeLookupUri(String channel, String token) throws InvalidInputException {
        if (!channel.contains("https://www.youtube.com/")) {
            throw new InvalidInputException("Not youtube url");
        }
        String lookupUri = "https://commentpicker.com/actions/youtube-channel-id.php?url=https%3A%2F%2Fwww.googleapis.com" +
                "%2Fyoutube%2Fv3%2Fchannels%3Fpart%3Did%2Csnippet%2Cstatistics%2CcontentDetails%2Cstatus";
        String idOrUsernamePrefix = channel.contains("/channel/") ? "%26id%3D" : "%26forUsername%3D";
        return lookupUri + idOrUsernamePrefix + channel.split("/")[channel.split("/").length-1] +
                "&token=" + token;
    }

    // TODO: Replace with client to handle ratelimiting well enough to allow scheduling
    public static List<String> getE621Posts(String search) throws IOException, NoSuchResourceException {
        String baseUrl = "https://e621.net/posts.json?tags=";
        String limit = "&limit=250";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(baseUrl + search + limit);
            HttpResponse response = client.execute(get);
            try {
                JSONObject jsonResponse = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
                JSONArray posts = jsonResponse.getJSONArray("posts");
                if (posts.length() == 0) {
                    throw new NoSuchResourceException("No posts were found for that search");
                }
                ArrayList<String> images = new ArrayList<>();
                for (int i = 0; i < posts.length(); i++) {
                    try {
                        images.add(posts.getJSONObject(i).getJSONObject("file").getString("url"));
                    } catch (Exception ignored) {
                        // Null url to image, we can generate our own with the md5 hash and file ext
                        try {
                            images.add(buildE621StaticPath(posts.getJSONObject(i).getJSONObject("file")));
                        } catch (Exception ignored2) {
                            // If that attempt fails, just skip
                        }
                    }
                }
                if (images.isEmpty())
                    throw new NoSuchResourceException("Could not find results for tags");
                return images;
            } catch (JSONException e) {
                logger.severe("Failed to parse e621 response", e);
                throw new NoSuchResourceException("Could not find any results for tags");
            }
        } catch (IOException e) {
            logger.warning("Exception getting e621 post", e);
            throw e;
        }
    }

    private static String buildE621StaticPath(JSONObject jsonObject) {
        String hash = jsonObject.getString("md5");
        return "https://static1.e621.net/data/" + hash.substring(0,2) + "/" + hash.substring(2,4) + "/" +
                hash + "." + jsonObject.getString("ext");
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

        }
        catch (Exception e) {
            logger.severe("Failed to get neuralhash response", e);
            // In this case just return blank to allow image through
            return "";
        }
    }
}
